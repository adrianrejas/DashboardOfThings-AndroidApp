package com.arejas.dashboardofthings.data.interfaces;

import android.content.Context;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.arejas.dashboardofthings.data.helpers.DataHelper;
import com.arejas.dashboardofthings.data.sources.database.DotDatabase;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.LiveDataResource;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.ui.notifications.NotificationsHelper;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.rx.RxHelper;

import org.jetbrains.annotations.NotNull;

import java.util.List;
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

    LiveData<Resource<List<NetworkExtended>>> networkList;
    LiveData<Resource<List<SensorExtended>>> sensorList;
    LiveData<Resource<List<SensorExtended>>> sensorListMainDashboard;
    LiveData<Resource<List<SensorExtended>>> sensorListLocated;
    LiveData<Resource<List<ActuatorExtended>>> actuatorList;
    LiveData<Resource<List<ActuatorExtended>>> actuatorListMainDashboard;
    LiveData<Resource<List<ActuatorExtended>>> actuatorListLocated;
    LiveData<Resource<List<DataValue>>> allSensorsInMainDashboardLastValues;

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

    public LiveData<Resource<List<NetworkExtended>>> getListOfNetworks() {
        try {
            if (this.networkList == null) {
                Enumerators.ElementType[] elementTypes = new Enumerators.ElementType[1];
                elementTypes[0] = Enumerators.ElementType.NETWORK;
                Enumerators.LogLevel[] logLevels = new Enumerators.LogLevel[2];
                logLevels[0] = Enumerators.LogLevel.WARN;
                logLevels[1] = Enumerators.LogLevel.ERROR;
                this.networkList = new LiveDataResource<List<NetworkExtended>>(() -> this.dotDatabase.networksDao().getAllExtended(elementTypes, logLevels));
            }
            return this.networkList;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<NetworkExtended>> getNetwork(@NotNull Integer networkId) {
        Enumerators.ElementType[] elementTypes = new Enumerators.ElementType[1];
        elementTypes[0] = Enumerators.ElementType.NETWORK;
        Enumerators.LogLevel[] logLevels = new Enumerators.LogLevel[2];
        logLevels[0] = Enumerators.LogLevel.WARN;
        logLevels[1] = Enumerators.LogLevel.ERROR;
        return new LiveDataResource<NetworkExtended>(() -> this.dotDatabase.networksDao().findExtendedById(networkId, elementTypes, logLevels));
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

    public LiveData<Resource<List<SensorExtended>>> getListOfSensors() {
        try {
            if (this.sensorList == null) {
                Enumerators.ElementType[] elementTypes = new Enumerators.ElementType[1];
                elementTypes[0] = Enumerators.ElementType.SENSOR;
                Enumerators.LogLevel[] logLevels = new Enumerators.LogLevel[2];
                logLevels[0] = Enumerators.LogLevel.WARN;
                logLevels[1] = Enumerators.LogLevel.ERROR;
                this.sensorList = new LiveDataResource<List<SensorExtended>>(() -> this.dotDatabase.sensorsDao().getAllExtended(elementTypes, logLevels));
            }
            return this.sensorList;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<List<Sensor>>> getListOfSensorsFromSameNetwork(int networkId) {
        try {
            return new LiveDataResource<List<Sensor>>(() -> this.dotDatabase.sensorsDao().getAllFromSameNetwork(networkId));
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<List<SensorExtended>>> getListOfSensorsMainDashboard() {
        try {
            if (this.sensorListMainDashboard == null) {
                Enumerators.ElementType[] elementTypes = new Enumerators.ElementType[1];
                elementTypes[0] = Enumerators.ElementType.SENSOR;
                Enumerators.LogLevel[] logLevels = new Enumerators.LogLevel[2];
                logLevels[0] = Enumerators.LogLevel.WARN;
                logLevels[1] = Enumerators.LogLevel.ERROR;
                this.sensorListMainDashboard = new LiveDataResource<List<SensorExtended>>(() -> this.dotDatabase.sensorsDao().getAllExtendedToBeShownInMainDashboard(elementTypes, logLevels));
            }
            return this.sensorListMainDashboard;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<List<SensorExtended>>> getListOfSensorsLocated() {
        try {
            if (this.sensorListLocated == null) {
                Enumerators.ElementType[] elementTypes = new Enumerators.ElementType[1];
                elementTypes[0] = Enumerators.ElementType.SENSOR;
                Enumerators.LogLevel[] logLevels = new Enumerators.LogLevel[2];
                logLevels[0] = Enumerators.LogLevel.WARN;
                logLevels[1] = Enumerators.LogLevel.ERROR;
                this.sensorListLocated = new LiveDataResource<List<SensorExtended>>(() -> this.dotDatabase.sensorsDao().getAllExtendedLocated(elementTypes, logLevels));
            }
            return this.sensorListLocated;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<SensorExtended>> getSensor(@NotNull Integer sensorId) {
        try {
            Enumerators.ElementType[] elementTypes = new Enumerators.ElementType[1];
            elementTypes[0] = Enumerators.ElementType.SENSOR;
            Enumerators.LogLevel[] logLevels = new Enumerators.LogLevel[2];
            logLevels[0] = Enumerators.LogLevel.WARN;
            logLevels[1] = Enumerators.LogLevel.ERROR;
            return new LiveDataResource<SensorExtended>(() -> this.dotDatabase.sensorsDao().findByIdExtended(sensorId, elementTypes, logLevels));
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

    public LiveData<Resource<List<ActuatorExtended>>> getListOfActuators() {
        try {
            if (this.actuatorList == null) {
                Enumerators.ElementType[] elementTypes = new Enumerators.ElementType[1];
                elementTypes[0] = Enumerators.ElementType.ACTUATOR;
                Enumerators.LogLevel[] logLevels = new Enumerators.LogLevel[2];
                logLevels[0] = Enumerators.LogLevel.WARN;
                logLevels[1] = Enumerators.LogLevel.ERROR;
                this.actuatorList = new LiveDataResource<List<ActuatorExtended>>(() -> this.dotDatabase.actuatorsDao().getAllExtended(elementTypes, logLevels));
            }
            return this.actuatorList;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<List<Actuator>> getListOfActuatorsFromSameNetwork(int NetworkId) {
        try {
            return this.dotDatabase.actuatorsDao().getAllFromSameNetwork(NetworkId);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<List<ActuatorExtended>>> getListOfActuatorsMainDashboard() {
        try {
            if (this.actuatorListMainDashboard == null) {
                Enumerators.ElementType[] elementTypes = new Enumerators.ElementType[1];
                elementTypes[0] = Enumerators.ElementType.ACTUATOR;
                Enumerators.LogLevel[] logLevels = new Enumerators.LogLevel[2];
                logLevels[0] = Enumerators.LogLevel.WARN;
                logLevels[1] = Enumerators.LogLevel.ERROR;
                this.actuatorListMainDashboard = new LiveDataResource<List<ActuatorExtended>>(() -> this.dotDatabase.actuatorsDao().getAllExtendedToBeShownInMainDashboard(elementTypes, logLevels));
            }
            return this.actuatorListMainDashboard;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<List<ActuatorExtended>>> getListOfActuatorsLocated() {
        try {
            if (this.actuatorListLocated == null) {
                Enumerators.ElementType[] elementTypes = new Enumerators.ElementType[1];
                elementTypes[0] = Enumerators.ElementType.ACTUATOR;
                Enumerators.LogLevel[] logLevels = new Enumerators.LogLevel[2];
                logLevels[0] = Enumerators.LogLevel.WARN;
                logLevels[1] = Enumerators.LogLevel.ERROR;
                this.actuatorListLocated = new LiveDataResource<List<ActuatorExtended>>(() -> this.dotDatabase.actuatorsDao().getAllExtendedLocated(elementTypes, logLevels));
            }
            return this.actuatorListLocated;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<ActuatorExtended>> getActuator(@NotNull Integer actuatorId) {
        try {
            Enumerators.ElementType[] elementTypes = new Enumerators.ElementType[1];
            elementTypes[0] = Enumerators.ElementType.ACTUATOR;
            Enumerators.LogLevel[] logLevels = new Enumerators.LogLevel[2];
            logLevels[0] = Enumerators.LogLevel.WARN;
            logLevels[1] = Enumerators.LogLevel.ERROR;
            return new LiveDataResource<ActuatorExtended>(() -> this.dotDatabase.actuatorsDao().findByIdExtended(actuatorId, elementTypes, logLevels));
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

    /* Functions for managing users request to reload or update info */

    public LiveData<Resource> requestSensorReload(@NotNull Sensor sensor) {
        MutableLiveData<Resource> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        try {
            RxHelper.publishSensorReloadRequest(sensor);
            result.postValue(Resource.success(null));
        } catch (Exception e) {
            result.postValue(Resource.error(e, null));
        }
        return result;
    }

    public LiveData<Resource> updateActuatorData(@NotNull Actuator actuator, @NotNull String data) {
        MutableLiveData<Resource> result = new MutableLiveData<>();
        result.postValue(Resource.loading(null));
        try {
            RxHelper.publishActuatorUpdate(new Pair<>(actuator, data));
            result.postValue(Resource.success(null));
        } catch (Exception e) {
            result.postValue(Resource.error(e, null));
        }
        return result;
    }

    /* Functions for managing request operations over data values stored */

    public LiveData<Resource<List<DataValue>>> getLastValuesFromAllMainDashboard() {
        try {
            if (this.allSensorsInMainDashboardLastValues == null) {
                this.allSensorsInMainDashboardLastValues = new LiveDataResource<List<DataValue>>(() -> this.dotDatabase.dataValuesDao().getLastValuesForAllInMainDashboard());
            }
            return this.allSensorsInMainDashboardLastValues;
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<DataValue> findLastValuesForSensorId(int id) {
        try {
            return this.dotDatabase.dataValuesDao().findLastForSensorId(id);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<DataValue> findLastValuesForSensorIds(int[] ids) {
        try {
            return this.dotDatabase.dataValuesDao().findLastForSensorIds(ids);
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<List<DataValue>>> getLastValuesForSensorId(int id) {
        try {
            return new LiveDataResource<List<DataValue>>(() -> this.dotDatabase.dataValuesDao().getLastValuesForSensorId(id));
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<List<DataValue>>> getAvgLastOneDayValuesForSensorId(int id) {
        try {
            return new LiveDataResource<List<DataValue>>(() -> this.dotDatabase.dataValuesDao().getAvgLastOneDayValuesForSensorId(id));
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<List<DataValue>>> getAvgLastOneWeekValuesForSensorId(int id) {
        try {
            return new LiveDataResource<List<DataValue>>(() -> this.dotDatabase.dataValuesDao().getAvgLastOneWeekValuesForSensorId(id));
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<List<DataValue>>> getAvgLastOneMonthValuesForSensorId(int id) {
        try {
            return new LiveDataResource<List<DataValue>>(() -> this.dotDatabase.dataValuesDao().getAvgLastOneMonthValuesForSensorId(id));
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<List<DataValue>>> getAvgLastOneYearValuesForSensorId(int id) {
        try {
            return new LiveDataResource<List<DataValue>>(() -> this.dotDatabase.dataValuesDao().getAvgLastOneYearValuesForSensorId(id));
        } catch (Exception e) {
            return null;
        }
    }

    /* Functions for managing request operations over logs stored */

    public LiveData<Resource<List<Log>>> getLastConfigurationLogs() {
        try {
            Enumerators.LogLevel[] logLevels = new Enumerators.LogLevel[3];
            logLevels[0] = Enumerators.LogLevel.INFO;
            logLevels[1] = Enumerators.LogLevel.WARN;
            logLevels[2] = Enumerators.LogLevel.ERROR;
            return new LiveDataResource<List<Log>>(() -> this.dotDatabase.logsDao().getAllLastHundredLogs(logLevels));
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Resource<List<Log>>> getLastNotificationLogsInMainDashboard() {
        try {
            Enumerators.LogLevel[] logLevels = new Enumerators.LogLevel[2];
            logLevels[0] = Enumerators.LogLevel.NOTIF_WARN;
            logLevels[1] = Enumerators.LogLevel.NOTIF_CRITICAL;
            return new LiveDataResource<List<Log>>(() -> this.dotDatabase.logsDao().getLastLogForSensorElementsInMainDashboard(logLevels));
        } catch (Exception e) {
            return null;
        }
    }

    public LiveData<Log> getLastNotificationLogForElement(int elementId,
                                                                Enumerators.ElementType elementType) {
        try {
            Enumerators.LogLevel[] logLevels = new Enumerators.LogLevel[3];
            logLevels[0] = Enumerators.LogLevel.NOTIF_NONE;
            logLevels[1] = Enumerators.LogLevel.NOTIF_WARN;
            logLevels[2] = Enumerators.LogLevel.NOTIF_CRITICAL;
            return this.dotDatabase.logsDao().findLastForElementId(elementId,
                    elementType, logLevels);
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

    /* Static functions to be used outside */

    public static void checkThresholdsForDataReceived(Context context, Sensor sensor, String dataReceived){
        try {
            Enumerators.NotificationState state = DataHelper.getNotificationStatus(dataReceived,
                    sensor.getDataType(), sensor.getThresholdAboveWarning(), sensor.getThresholdAboveCritical(),
                    sensor.getThresholdBelowWarning(), sensor.getThresholdBelowCritical(),
                    sensor.getThresholdEqualsWarning(), sensor.getThresholdEqualsCritical());
            if (state != null) {
                switch (state) {
                    case NONE:
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.NONE);
                        break;
                    case WARNING:
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.WARNING);
                        break;
                    case CRITICAL:
                        NotificationsHelper.processStateNotificationForSensor(context, sensor, Enumerators.NotificationType.CRITICAL);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
