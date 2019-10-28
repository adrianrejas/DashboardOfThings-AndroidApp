package com.arejas.dashboardofthings.presentation.interfaces.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
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
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapViewModel extends AndroidViewModel {

    private final SensorManagementUseCase sensorManagementUseCase;
    private final ActuatorManagementUseCase actuatorManagementUseCase;

    private LiveData<Resource<List<SensorExtended>>> sensorsLocated;
    private LiveData<Resource<List<ActuatorExtended>>> actuatorsLocated;

    public MapViewModel(@NonNull Application application,
                        SensorManagementUseCase sensorManagementUseCase,
                        ActuatorManagementUseCase actuatorManagementUseCase) {
        super(application);
        this.sensorManagementUseCase = sensorManagementUseCase;
        this.actuatorManagementUseCase = actuatorManagementUseCase;
    }

    public LiveData<Resource<List<SensorExtended>>> getListOfSensorsLocated(boolean refreshData) {
        if (refreshData) sensorsLocated = null;
        if (sensorsLocated == null) {
            sensorsLocated = this.sensorManagementUseCase.getListOfSensorsLocated();
        }
        return sensorsLocated;
    }

    public LiveData<Resource<List<ActuatorExtended>>> getListOfActuatorsLocated(boolean refreshData) {
        if (refreshData) actuatorsLocated = null;
        if (actuatorsLocated == null) {
            actuatorsLocated = this.actuatorManagementUseCase.getListOfActuatorsLocated();
        }
        return actuatorsLocated;
    }

}
