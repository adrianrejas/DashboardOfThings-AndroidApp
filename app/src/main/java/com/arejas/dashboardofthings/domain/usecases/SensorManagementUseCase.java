package com.arejas.dashboardofthings.domain.usecases;

import androidx.lifecycle.LiveData;

import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SensorManagementUseCase extends BaseUseCase {
    
    public LiveData<Resource<List<SensorExtended>>> getListOfSensors();

    public LiveData<Resource<List<SensorExtended>>> getListOfSensorsMainDashboard();

    public LiveData<Resource<List<SensorExtended>>> getListOfSensorsLocated();

    public LiveData<Resource<SensorExtended>> getSensor(@NotNull Integer sensorId);

    public LiveData<Resource> createSensor(@NotNull Sensor sensor);

    public LiveData<Resource> updateSensor(@NotNull Sensor sensor);

    public LiveData<Resource> deleteSensor(@NotNull Sensor sensor);
    
}
