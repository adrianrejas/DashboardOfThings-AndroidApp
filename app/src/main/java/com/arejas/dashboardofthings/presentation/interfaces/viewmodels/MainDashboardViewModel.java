package com.arejas.dashboardofthings.presentation.interfaces.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainDashboardViewModel extends AndroidViewModel {

    private final SensorManagementUseCase sensorManagementUseCase;
    private final ActuatorManagementUseCase actuatorManagementUseCase;
    private final DataManagementUseCase dataManagementUseCase;
    private final LogsManagementUseCase logsManagementUseCase;

    private LiveData<Resource<List<SensorExtended>>> sensorsInDashboard;
    private LiveData<Resource<List<ActuatorExtended>>> actuatorsInDashboard;
    private LiveData<Resource<List<Log>>> sensorNotificationsInDashboard;
    private LiveData<Resource<List<Log>>> logsInDashboard;
    private LiveData<Resource<List<DataValue>>> sensorsInDashboardLastValues;

    private Map<Integer,LiveData<Resource<List<DataValue>>>> mapSensorLastValuesLiveData;
    private Map<Integer,LiveData<Resource<List<DataValue>>>> mapSensorLastHourLiveData;
    private Map<Integer,LiveData<Resource<List<DataValue>>>> mapSensorLastWeekLiveData;
    private Map<Integer,LiveData<Resource<List<DataValue>>>> mapSensorLastMonthLiveData;
    private Map<Integer,LiveData<Resource<List<DataValue>>>> mapSensorLastYearLiveData;

    public MainDashboardViewModel(@NonNull Application application,
                                  SensorManagementUseCase sensorManagementUseCase,
                                  ActuatorManagementUseCase actuatorManagementUseCase,
                                  DataManagementUseCase dataManagementUseCase,
                                  LogsManagementUseCase logsManagementUseCase) {
        super(application);
        this.sensorManagementUseCase = sensorManagementUseCase;
        this.actuatorManagementUseCase = actuatorManagementUseCase;
        this.dataManagementUseCase = dataManagementUseCase;
        this.logsManagementUseCase = logsManagementUseCase;
        mapSensorLastValuesLiveData=new HashMap<>();
        mapSensorLastHourLiveData=new HashMap<>();
        mapSensorLastWeekLiveData=new HashMap<>();
        mapSensorLastMonthLiveData=new HashMap<>();
        mapSensorLastYearLiveData=new HashMap<>();
    }

    public LiveData<Resource<List<SensorExtended>>> getListOfSensorsMainDashboard(boolean refreshData) {
        if (refreshData) sensorsInDashboard = null;
        if (sensorsInDashboard == null) {
            sensorsInDashboard = this.sensorManagementUseCase.getListOfSensorsMainDashboard();
        }
        return sensorsInDashboard;
    }

    public LiveData<Resource> requestSensorReload(Sensor sensor) {
        return this.dataManagementUseCase.requestSensorReload(sensor);
    }

    public LiveData<Resource<List<ActuatorExtended>>> getListOfActuatorsMainDashboard(boolean refreshData) {
        if (refreshData) actuatorsInDashboard = null;
        if (actuatorsInDashboard == null) {
            actuatorsInDashboard = this.actuatorManagementUseCase.getListOfActuatorsMainDashboard();
        }
        return actuatorsInDashboard;
    }

    public LiveData<Resource<List<DataValue>>> getListOfSensorsInDashboardLastValues(boolean refreshData) {
        if (refreshData) sensorsInDashboardLastValues = null;
        if (sensorsInDashboardLastValues == null) {
            sensorsInDashboardLastValues = this.dataManagementUseCase.getLastValuesFromAllMainDashboard();
        }
        return sensorsInDashboardLastValues;
    }

    public LiveData<Resource> sendActuatorData(Actuator actuator, String data) {
        return this.dataManagementUseCase.updateActuatorData(actuator, data);
    }

    public LiveData<Resource<List<DataValue>>> getLastValuesForSensorId(int id) {
        LiveData<Resource<List<DataValue>>> returningLiveData = mapSensorLastValuesLiveData.get(id);
        if ((returningLiveData == null) || (!returningLiveData.getValue().getStatus().equals(Resource.Status.SUCCESS))) {
            returningLiveData = this.dataManagementUseCase.getLastValuesForSensorId(id);
            if (returningLiveData != null)
                mapSensorLastValuesLiveData.put(id, returningLiveData);
        }
        return returningLiveData;
    }

    public LiveData<Resource<List<DataValue>>> getAvgLastOneDayValuesForSensorId(int id) {
        LiveData<Resource<List<DataValue>>> returningLiveData = mapSensorLastHourLiveData.get(id);
        if ((returningLiveData == null) || (!returningLiveData.getValue().getStatus().equals(Resource.Status.SUCCESS))) {
            returningLiveData = this.dataManagementUseCase.getAvgLastOneDayValuesForSensorId(id);
            if (returningLiveData != null)
                mapSensorLastHourLiveData.put(id, returningLiveData);
        }
        return returningLiveData;
    }

    public LiveData<Resource<List<DataValue>>> getAvgLastOneWeekValuesForSensorId(int id) {
        LiveData<Resource<List<DataValue>>> returningLiveData = mapSensorLastWeekLiveData.get(id);
        if ((returningLiveData == null) || (!returningLiveData.getValue().getStatus().equals(Resource.Status.SUCCESS))) {
            returningLiveData = this.dataManagementUseCase.getAvgLastOneWeekValuesForSensorId(id);
            if (returningLiveData != null)
                mapSensorLastWeekLiveData.put(id, returningLiveData);
        }
        return returningLiveData;
    }

    public LiveData<Resource<List<DataValue>>> getAvgLastOneMonthValuesForSensorId(int id) {
        LiveData<Resource<List<DataValue>>> returningLiveData = mapSensorLastMonthLiveData.get(id);
        if ((returningLiveData == null) || (!returningLiveData.getValue().getStatus().equals(Resource.Status.SUCCESS))) {
            returningLiveData = this.dataManagementUseCase.getAvgLastOneMonthValuesForSensorId(id);
            if (returningLiveData != null)
                mapSensorLastMonthLiveData.put(id, returningLiveData);
        }
        return returningLiveData;
    }

    public LiveData<Resource<List<DataValue>>> getAvgLastOneYearValuesForSensorId(int id) {
        LiveData<Resource<List<DataValue>>> returningLiveData = mapSensorLastYearLiveData.get(id);
        if ((returningLiveData == null) || (!returningLiveData.getValue().getStatus().equals(Resource.Status.SUCCESS))) {
            returningLiveData = this.dataManagementUseCase.getAvgLastOneYearValuesForSensorId(id);
            if (returningLiveData != null)
                mapSensorLastYearLiveData.put(id, returningLiveData);
        }
        return returningLiveData;
    }

    public LiveData<Resource<List<Log>>> getLastConfigurationLogs(boolean refreshData) {
        if (refreshData) logsInDashboard = null;
        if (logsInDashboard == null) {
            logsInDashboard = this.logsManagementUseCase.getLastConfigurationLogs();
        }
        return logsInDashboard;
    }

    public LiveData<Resource<List<Log>>> getLastSensorNotificationLogsInMainDashboard(boolean refreshData) {
        if (refreshData) sensorNotificationsInDashboard = null;
        if (sensorNotificationsInDashboard == null) {
            sensorNotificationsInDashboard = this.logsManagementUseCase.getLastNotificationLogsForSensorsInMainDashboard();
        }
        return sensorNotificationsInDashboard;
    }

}
