package com.arejas.dashboardofthings.domain.usecases.implementations;

import androidx.lifecycle.LiveData;

import com.arejas.dashboardofthings.data.interfaces.DotRepository;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.entities.widget.SensorWidgetItem;
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase;
import com.arejas.dashboardofthings.utils.functional.Consumer;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SensorManagementUseCaseImpl implements SensorManagementUseCase {

    private final DotRepository repository;

    public SensorManagementUseCaseImpl(DotRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public LiveData<Resource<List<SensorExtended>>> getListOfSensors() {
        return repository.getListOfSensors();
    }

    public LiveData<Resource<List<SensorExtended>>> getListOfSensorsMainDashboard() {
        return repository.getListOfSensorsMainDashboard();
    }

    public LiveData<Resource<List<SensorExtended>>> getListOfSensorsLocated() {
        return repository.getListOfSensorsLocated();
    }

    @Override
    public LiveData<Resource<SensorExtended>> getSensor(@NotNull Integer sensorId) {
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

    @Override
    public void getSensorInfoForWidget(@NotNull int id, Consumer<SensorWidgetItem> consumer) {
        repository.getSensorInfoForWidget(id, consumer);
    }

    @Override
    public void getSensorInfoForWidgets(@NotNull int[] ids, Consumer<List<SensorWidgetItem>> consumer) {
        repository.getSensorInfoForWidgets(ids, consumer);
    }

    @Override
    public void requestSensorReload(@NotNull int id) {
        repository.requestSensorReloadInstant(id);
    }
}
