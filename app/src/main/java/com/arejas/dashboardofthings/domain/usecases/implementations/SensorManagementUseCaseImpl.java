package com.arejas.dashboardofthings.domain.usecases.implementations;

import androidx.lifecycle.LiveData;

import com.arejas.dashboardofthings.data.interfaces.DotRepository;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SensorManagementUseCaseImpl implements SensorManagementUseCase {

    private final DotRepository repository;

    public SensorManagementUseCaseImpl(DotRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public LiveData<List<SensorExtended>> getListOfSensors() {
        return repository.getListOfSensors();
    }

    @Override
    public LiveData<SensorExtended> getSensor(@NotNull Integer sensorId) {
        return repository.getSensor(sensorId);
    }

    @Override
    public LiveData<Resource> createSensor(@NotNull Sensor sensor) {
        return repository.createSensor(sensor);
    }

    @Override
    public LiveData<Resource> updateSensor(@NotNull Sensor sensor) {
        return repository.updateSensor(sensor);
    }

    @Override
    public LiveData<Resource> deleteSensor(@NotNull Sensor sensor) {
        return repository.deleteSensor(sensor);
    }
}
