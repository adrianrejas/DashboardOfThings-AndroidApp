package com.arejas.dashboardofthings.data.interfaces;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.arejas.dashboardofthings.data.sources.database.DotDatabase;
import com.arejas.dashboardofthings.data.sources.network.HttpNetworkInterfaceHelper;
import com.arejas.dashboardofthings.data.sources.network.MqttNetworkInterfaceHelper;
import com.arejas.dashboardofthings.data.sources.network.NetworkInterfaceHelper;
import com.arejas.dashboardofthings.domain.entities.Actuator;
import com.arejas.dashboardofthings.domain.entities.DataValue;
import com.arejas.dashboardofthings.domain.entities.Log;
import com.arejas.dashboardofthings.domain.entities.Network;
import com.arejas.dashboardofthings.domain.entities.Sensor;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.rx.RxHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
public class DotRepository {

    Context appContext;
    DotDatabase dotDatabase;
    Executor dbExecutorManagement;
    Executor dbExecutorDataInsert;

    LiveData<List<Network>> networkList;
    LiveData<List<Sensor>> sensorList;
    LiveData<List<Actuator>> actuatorList;

    Map<Integer, NetworkInterfaceHelper> networkHelpers;

    private final Object networkHelpersLock = new Object();

    @Inject
    public DotRepository(DotDatabase dotDatabase,
                         @Named("dbExecutorManagement") Executor dbExecutorManagement,
                         @Named("dbExecutorDataInsert") Executor dbExecutorDataInsert,
                         Context context) {
        this.dotDatabase = dotDatabase;
        this.dbExecutorManagement = dbExecutorManagement;
        this.dbExecutorDataInsert = dbExecutorDataInsert;
        this.appContext = context;
        this.networkHelpers = new HashMap<>();
        initializeNetworkHelpersAtBeginning();
        initializeSubscriptionsToDataValuesAndLogs();
    }

    /* Functions to be executed when started the repository */

    private void initializeNetworkHelpersAtBeginning() {
        getListOfNetworks().observeForever(networks -> {
            for (Network network : networks) {
                initNetworkHelper(network);
            }
            getListOfSensors().observeForever(new Observer<List<Sensor>>() {
                @Override
                public void onChanged(List<Sensor> sensors) {
                    for (Sensor sensor : sensors) {
                        registerSensorInNetwork(sensor);
                    }
                }
            });
        });
    }

    private void initializeSubscriptionsToDataValuesAndLogs() {
        RxHelper.subscribeToAllSensorsData(dataValue -> {
            dbExecutorDataInsert.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        dotDatabase.dataValuesDao().insert(dataValue);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        RxHelper.subscribeToAllLogs(log -> {
            dbExecutorDataInsert.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        dotDatabase.logsDao().insert(log);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    /* Functions for managing the CRUD operations over networks */

    public LiveData<List<Network>> getListOfNetworks() {
        try {
            if (this.networkList == null) {
                this.networkList = this.dotDatabase.networksDao().getAll();
            }
            return this.networkList;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Network> getNetwork(@NotNull Integer networkId) {
        return this.dotDatabase.networksDao().findById(networkId);
    }

    public LiveData<Resource> createNetwork(@NotNull Network network) {
        MutableLiveData<Resource> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        dbExecutorManagement.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dotDatabase.networksDao().insert(network);
                    initNetworkHelper(network);
                    result.postValue(Resource.success(null));
                } catch (Exception e) {
                    result.postValue(Resource.error(e, null));
                }
            }
        });
        return result;
    }

    public LiveData<Resource> updateNetwork(@NotNull Network network) {
        MutableLiveData<Resource> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        dbExecutorManagement.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dotDatabase.networksDao().update(network);
                    dotDatabase.logsDao().updateElementName(network.getId(),
                            Enumerators.ElementType.NETWORK, network.getName());
                    restartNetworkHelper(network);
                    result.postValue(Resource.success(null));
                } catch (Exception e) {
                    result.postValue(Resource.error(e, null));
                }
            }
        });
        return result;
    }

    public LiveData<Resource> deleteNetwork(@NotNull Network network) {
        MutableLiveData<Resource> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        dbExecutorManagement.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dotDatabase.networksDao().delete(network);
                    dotDatabase.logsDao().deleteAll(network.getId(), Enumerators.ElementType.NETWORK);
                    closeNetworkHelper(network);
                    result.postValue(Resource.success(null));
                } catch (Exception e) {
                    result.postValue(Resource.error(e, null));
                }
            }
        });
        return result;
    }

    private boolean initNetworkHelper(@NotNull Network network) {
        synchronized (networkHelpersLock) {
            closeNetworkHelper(network);
            NetworkInterfaceHelper helper = null;
            switch (network.getNetworkType()) {
                case HTTP:
                    helper = new HttpNetworkInterfaceHelper(network);
                    break;
                case MQTT:
                    helper = new MqttNetworkInterfaceHelper(network);
                    break;
            }
            if (helper != null) {
                List<Sensor> initialSensors = new ArrayList<>();
                for (Sensor sensor : sensorList.getValue()) {
                    if (sensor.getNetworkId().equals(network.getId())) {
                        initialSensors.add(sensor);
                    }
                }
                helper.initNetworkInterface(appContext,
                        initialSensors.toArray(new Sensor[initialSensors.size()]));
                this.networkHelpers.put(network.getId(), helper);
                return true;
            }
            return false;
        }
    }

    private boolean closeNetworkHelper(@NotNull Network network) {
        synchronized (networkHelpersLock) {
            if (this.networkHelpers.containsKey(network.getId())) {
                this.networkHelpers.get(network.getId()).closeNetworkInterface(appContext);
                this.networkHelpers.remove(network.getId());
            }
            return true;
        }
    }

    private boolean restartNetworkHelper(@NotNull Network network) {
        synchronized (networkHelpersLock) {
            closeNetworkHelper(network);
            initNetworkHelper(network);
            return true;
        }
    }

    /* Functions for managing the CRUD operations over sensors */

    public LiveData<List<Sensor>> getListOfSensors() {
        try {
            if (this.sensorList == null) {
                this.sensorList = this.dotDatabase.sensorsDao().getAll();
            }
            return this.sensorList;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Sensor> getSensor(@NotNull Integer sensorId) {
        try {
            return this.dotDatabase.sensorsDao().findById(sensorId);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource> createSensor(@NotNull Sensor sensor) {
        MutableLiveData<Resource> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        dbExecutorManagement.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dotDatabase.sensorsDao().insert(sensor);
                    registerSensorInNetwork(sensor);
                    result.postValue(Resource.success(null));
                } catch (Exception e) {
                    result.postValue(Resource.error(e, null));
                }
            }
        });
        return result;
    }

    public LiveData<Resource> updateSensor(@NotNull Sensor sensor) {
        MutableLiveData<Resource> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        dbExecutorManagement.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dotDatabase.sensorsDao().update(sensor);
                    dotDatabase.logsDao().updateElementName(sensor.getId(),
                            Enumerators.ElementType.SENSOR, sensor.getName());
                    restartyRegisterSensorInNetwork(sensor);
                    result.postValue(Resource.success(null));
                } catch (Exception e) {
                    result.postValue(Resource.error(e, null));
                }
            }
        });
        return result;
    }

    public LiveData<Resource> deleteSensor(@NotNull Sensor sensor) {
        MutableLiveData<Resource> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        dbExecutorManagement.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dotDatabase.sensorsDao().delete(sensor);
                    dotDatabase.logsDao().deleteAll(sensor.getId(), Enumerators.ElementType.SENSOR);
                    unregisterSensorInNetwork(sensor);
                    result.postValue(Resource.success(null));
                } catch (Exception e) {
                    result.postValue(Resource.error(e, null));
                }
            }
        });
        return result;
    }

    private boolean registerSensorInNetwork(@NotNull Sensor sensor) {
        synchronized (networkHelpersLock) {
            if (networkHelpers.containsKey(sensor.getNetworkId())) {
                return networkHelpers.get(sensor.getNetworkId()).configureSensorReceiving(appContext, sensor);
            }
            return false;
        }
    }

    private boolean unregisterSensorInNetwork(@NotNull Sensor sensor) {
        synchronized (networkHelpersLock) {
            if (networkHelpers.containsKey(sensor.getNetworkId())) {
                return networkHelpers.get(sensor.getNetworkId()).unconfigureSensorReceiving(appContext, sensor);
            }
            return false;
        }
    }

    private boolean restartyRegisterSensorInNetwork(@NotNull Sensor sensor) {
        synchronized (networkHelpersLock) {
            if (unregisterSensorInNetwork(sensor)) {
                return registerSensorInNetwork(sensor);
            }
            return false;
        }
    }

    /* Functions for managing the CRUD operations over actuators */

    public LiveData<List<Actuator>> getListOfActuators() {
        try {
            if (this.actuatorList == null) {
                this.actuatorList = this.dotDatabase.actuatorsDao().getAll();
            }
            return this.actuatorList;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Actuator> getActuator(@NotNull Integer actuatorId) {
        try {
            return this.dotDatabase.actuatorsDao().findById(actuatorId);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource> createActuator(@NotNull Actuator actuator) {
        MutableLiveData<Resource> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        dbExecutorManagement.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dotDatabase.actuatorsDao().insert(actuator);
                    result.postValue(Resource.success(null));
                } catch (Exception e) {
                    result.postValue(Resource.error(e, null));
                }
            }
        });
        return result;
    }

    public LiveData<Resource> updateActuator(@NotNull Actuator actuator) {
        MutableLiveData<Resource> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        dbExecutorManagement.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dotDatabase.actuatorsDao().update(actuator);
                    dotDatabase.logsDao().updateElementName(actuator.getId(),
                            Enumerators.ElementType.ACTUATOR, actuator.getName());
                    result.postValue(Resource.success(null));
                } catch (Exception e) {
                    result.postValue(Resource.error(e, null));
                }
            }
        });
        return result;
    }

    public LiveData<Resource> deleteActuator(@NotNull Actuator actuator) {
        MutableLiveData<Resource> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        dbExecutorManagement.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dotDatabase.actuatorsDao().delete(actuator);
                    dotDatabase.logsDao().deleteAll(actuator.getId(), Enumerators.ElementType.ACTUATOR);
                    result.postValue(Resource.success(null));
                } catch (Exception e) {
                    result.postValue(Resource.error(e, null));
                }
            }
        });
        return result;
    }

    /* Functions for managing request operations over data values stored */

    public LiveData<List<DataValue>> getAll() {
        try {
            return this.dotDatabase.dataValuesDao().getAll();
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<DataValue> findLastForSensorId(int id) {
        try {
            return this.dotDatabase.dataValuesDao().findLastForSensorId(id);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<DataValue> findLastForSensorIds(int[] ids) {
        try {
            return this.dotDatabase.dataValuesDao().findLastForSensorIds(ids);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<DataValue>> getLastValuesForSensorId(int id) {
        try {
            return this.dotDatabase.dataValuesDao().getLastValuesForSensorId(id);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<DataValue>> getAvgLastOneDayValuesForSensorId(int id) {
        try {
            return this.dotDatabase.dataValuesDao().getAvgLastOneDayValuesForSensorId(id);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<DataValue>> getAllLastOneDayValuesForSensorId(int id) {
        try {
            return this.dotDatabase.dataValuesDao().getAllLastOneDayValuesForSensorId(id);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<DataValue>> getAvgLastOneWeekValuesForSensorId(int id) {
        try {
            return this.dotDatabase.dataValuesDao().getAvgLastOneWeekValuesForSensorId(id);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<DataValue>> getAllLastOneWeekValuesForSensorId(int id) {
        try {
            return this.dotDatabase.dataValuesDao().getAllLastOneWeekValuesForSensorId(id);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<DataValue>> getAvgLastOneMonthValuesForSensorId(int id) {
        try {
            return this.dotDatabase.dataValuesDao().getAvgLastOneMonthValuesForSensorId(id);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<DataValue>> getAllLastOneMonthValuesForSensorId(int id) {
        try {
            return this.dotDatabase.dataValuesDao().getAllLastOneMonthValuesForSensorId(id);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<DataValue>> getAvgLastOneYearValuesForSensorId(int id) {
        try {
            return this.dotDatabase.dataValuesDao().getAvgLastOneYearValuesForSensorId(id);
        } catch (Exception e) {
            return null;
        }
    }

    /* Functions for managing request operations over logs stored */

    public LiveData<List<Log>> getLastLogs() {
        try {
            return this.dotDatabase.logsDao().getAllLastHundredLogs();
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<Log>> getLastLogsForElement(int id, Enumerators.ElementType elementType) {
        try {
            return this.dotDatabase.logsDao().getLastHundredLogsForElementId(id, elementType);
        } catch (Exception e) {
            return null;
        }
    }
}
