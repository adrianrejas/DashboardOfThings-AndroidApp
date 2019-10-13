package com.arejas.dashboardofthings.data.interfaces;

import android.content.Context;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.arejas.dashboardofthings.data.sources.database.DotDatabase;
import com.arejas.dashboardofthings.data.sources.network.HttpNetworkInterfaceHelper;
import com.arejas.dashboardofthings.data.sources.network.MqttNetworkInterfaceHelper;
import com.arejas.dashboardofthings.data.sources.network.NetworkInterfaceHelper;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.ui.notifications.NotificationsHelper;
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

    LiveData<List<NetworkExtended>> networkList;
    LiveData<List<SensorExtended>> sensorList;
    LiveData<List<ActuatorExtended>> actuatorList;

    @Inject
    public DotRepository(DotDatabase dotDatabase,
                         @Named("dbExecutorManagement") Executor dbExecutorManagement,
                         @Named("dbExecutorDataInsert") Executor dbExecutorDataInsert,
                         Context context) {
        this.dotDatabase = dotDatabase;
        this.dbExecutorManagement = dbExecutorManagement;
        this.dbExecutorDataInsert = dbExecutorDataInsert;
        this.appContext = context;
        initializeSubscriptionsToDataValuesAndLogs();
    }

    /* Functions to be executed when started the repository */

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

    public List<Network> getListOfNetworksBlocking() {
        try {
            return this.dotDatabase.networksDao().getAllBlocking();
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<NetworkExtended>> getListOfNetworks() {
        try {
            if (this.networkList == null) {
                this.networkList = this.dotDatabase.networksDao().getAllExtended();
            }
            return this.networkList;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<NetworkExtended> getNetwork(@NotNull Integer networkId) {
        return this.dotDatabase.networksDao().findExtendedById(networkId);
    }

    public LiveData<Resource> createNetwork(@NotNull Network network) {
        MutableLiveData<Resource> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        dbExecutorManagement.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    dotDatabase.networksDao().insert(network);
                    RxHelper.publishNetworkManagementChange(new Pair<>(network, Enumerators.ElementManagementFunction.CREATE));
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
                    dotDatabase.networksDao().updateExtended(network);
                    RxHelper.publishNetworkManagementChange(new Pair<>(network, Enumerators.ElementManagementFunction.UPDATE));
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
                    dotDatabase.networksDao().deleteExtended(network);
                    RxHelper.publishNetworkManagementChange(new Pair<>(network, Enumerators.ElementManagementFunction.DELETE));
                    result.postValue(Resource.success(null));
                } catch (Exception e) {
                    result.postValue(Resource.error(e, null));
                }
            }
        });
        return result;
    }

    /* Functions for managing the CRUD operations over sensors */

    public List<Sensor> getListOfSensorsBlocking() {
        try {
            return this.dotDatabase.sensorsDao().getAllBlocking();
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<SensorExtended>> getListOfSensors() {
        try {
            if (this.sensorList == null) {
                this.sensorList = this.dotDatabase.sensorsDao().getAllExtended();
            }
            return this.sensorList;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<SensorExtended> getSensor(@NotNull Integer sensorId) {
        try {
            return this.dotDatabase.sensorsDao().findByIdExtended(sensorId);
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
                    RxHelper.publishSensorManagementChange(new Pair<>(sensor, Enumerators.ElementManagementFunction.CREATE));
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
                    dotDatabase.sensorsDao().updateExtended(sensor);
                    RxHelper.publishSensorManagementChange(new Pair<>(sensor, Enumerators.ElementManagementFunction.UPDATE));
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
                    dotDatabase.sensorsDao().deleteExtended(sensor);
                    RxHelper.publishSensorManagementChange(new Pair<>(sensor, Enumerators.ElementManagementFunction.DELETE));
                    result.postValue(Resource.success(null));
                } catch (Exception e) {
                    result.postValue(Resource.error(e, null));
                }
            }
        });
        return result;
    }

    /* Functions for managing the CRUD operations over actuators */

    public LiveData<List<ActuatorExtended>> getListOfActuators() {
        try {
            if (this.actuatorList == null) {
                this.actuatorList = this.dotDatabase.actuatorsDao().getAllExtended();
            }
            return this.actuatorList;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<ActuatorExtended> getActuator(@NotNull Integer actuatorId) {
        try {
            return this.dotDatabase.actuatorsDao().findByIdExtended(actuatorId);
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
                    RxHelper.publishActuatorManagementChange(new Pair<>(actuator, Enumerators.ElementManagementFunction.CREATE));
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
                    dotDatabase.actuatorsDao().updateExtended(actuator);
                    RxHelper.publishActuatorManagementChange(new Pair<>(actuator, Enumerators.ElementManagementFunction.UPDATE));
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
                    dotDatabase.actuatorsDao().deleteExtended(actuator);
                    RxHelper.publishActuatorManagementChange(new Pair<>(actuator, Enumerators.ElementManagementFunction.DELETE));
                    result.postValue(Resource.success(null));
                } catch (Exception e) {
                    result.postValue(Resource.error(e, null));
                }
            }
        });
        return result;
    }

    /* Functions for managing actuator data to update */

    public LiveData<Resource> updateActuatorData(@NotNull Actuator actuator, @NotNull String data) {
        MutableLiveData<Resource> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        dbExecutorManagement.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    RxHelper.publishActuatorUpdate(new Pair<>(actuator, data));
                    result.postValue(Resource.success(null));
                } catch (Exception e) {
                    result.postValue(Resource.error(e, null));
                }
            }
        });
        return result;
    }

    /* Functions for managing request operations over data values stored */

    public LiveData<List<DataValue>> getLastFromAll() {
        try {
            return this.dotDatabase.dataValuesDao().getLastValuesForAll();
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

    public static void checkThresholdsForDataReceived(Context context, Sensor sensor, String dataReceived){
        try {
            if (sensor != null) {
                if (sensor.getDataType().equals(Enumerators.DataType.INTEGER)) {
                    int thresholdAboveWarning = ((sensor.getThresholdAboveWarning() != null) &&
                            (!sensor.getThresholdAboveWarning().isEmpty())) ?
                            Integer.valueOf(sensor.getThresholdAboveWarning()) : Integer.MAX_VALUE;
                    int thresholdAboveCritical = ((sensor.getThresholdAboveCritical() != null) &&
                            (!sensor.getThresholdAboveCritical().isEmpty())) ?
                            Integer.valueOf(sensor.getThresholdAboveCritical()) : Integer.MAX_VALUE;
                    int thresholdBelowWarning = ((sensor.getThresholdBelowWarning() != null) &&
                            (!sensor.getThresholdBelowWarning().isEmpty())) ?
                            Integer.valueOf(sensor.getThresholdBelowWarning()) : Integer.MIN_VALUE;
                    int thresholdBelowCritical = ((sensor.getThresholdBelowCritical() != null) &&
                            (!sensor.getThresholdBelowCritical().isEmpty())) ?
                            Integer.valueOf(sensor.getThresholdBelowCritical()) : Integer.MIN_VALUE;
                    int intValue = Integer.valueOf(dataReceived);
                    if ((intValue >= thresholdAboveCritical) || (intValue <= thresholdBelowCritical)) {
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.CRITICAL);
                    } else if ((intValue >= thresholdAboveWarning) || (intValue <= thresholdBelowWarning)) {
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.WARN);
                    } else {
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.NONE);
                    }
                } else if (sensor.getDataType().equals(Enumerators.DataType.DECIMAL)) {
                    float thresholdAboveWarning = ((sensor.getThresholdAboveWarning() != null) &&
                            (!sensor.getThresholdAboveWarning().isEmpty())) ?
                            Float.valueOf(sensor.getThresholdAboveWarning()) : Float.MAX_VALUE;
                    float thresholdAboveCritical = ((sensor.getThresholdAboveCritical() != null) &&
                            (!sensor.getThresholdAboveCritical().isEmpty())) ?
                            Float.valueOf(sensor.getThresholdAboveCritical()) : Float.MAX_VALUE;
                    float thresholdBelowWarning = ((sensor.getThresholdBelowWarning() != null) &&
                            (!sensor.getThresholdBelowWarning().isEmpty())) ?
                            Float.valueOf(sensor.getThresholdBelowWarning()) : Float.MIN_VALUE;
                    float thresholdBelowCritical = ((sensor.getThresholdBelowCritical() != null) &&
                            (!sensor.getThresholdBelowCritical().isEmpty())) ?
                            Float.valueOf(sensor.getThresholdBelowCritical()) : Float.MIN_VALUE;
                    float floatValue = Float.valueOf(dataReceived);
                    if ((floatValue >= thresholdAboveCritical) || (floatValue <= thresholdBelowCritical)) {
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.CRITICAL);
                    } else if ((floatValue >= thresholdAboveWarning) || (floatValue <= thresholdBelowWarning)) {
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.WARN);
                    } else {
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.NONE);
                    }
                } else if (sensor.getDataType().equals(Enumerators.DataType.BOOLEAN)) {
                    Boolean thresholdEqualsWarning = ((sensor.getThresholdEqualsWarning() != null) &&
                            (!sensor.getThresholdEqualsWarning().isEmpty())) ?
                            Boolean.valueOf(sensor.getThresholdEqualsWarning()) : null;
                    Boolean thresholdEqualsCritical = ((sensor.getThresholdEqualsCritical() != null) &&
                            (!sensor.getThresholdEqualsCritical().isEmpty())) ?
                            Boolean.valueOf(sensor.getThresholdEqualsCritical()) : null;
                    boolean booleanValue = Boolean.valueOf(dataReceived);
                    if ((thresholdEqualsCritical != null) || (booleanValue == thresholdEqualsCritical.booleanValue())) {
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.CRITICAL);
                    } else if ((thresholdEqualsWarning != null) || (booleanValue == thresholdEqualsWarning.booleanValue())) {
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.WARN);
                    } else {
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.NONE);
                    }
                } else  {
                    if ((sensor.getThresholdEqualsCritical() != null) || (dataReceived.equals(sensor.getThresholdEqualsCritical()))) {
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.CRITICAL);
                    } else if ((sensor.getThresholdEqualsWarning() != null) || (dataReceived.equals(sensor.getThresholdEqualsWarning()))) {
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.WARN);
                    } else {
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.NONE);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
