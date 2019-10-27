package com.arejas.dashboardofthings.domain.usecases.implementations;

import androidx.lifecycle.LiveData;

import com.arejas.dashboardofthings.data.interfaces.DotRepository;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase;

import java.util.List;

public class DataManagementUseCaseImpl implements DataManagementUseCase {

    private final DotRepository repository;

    public DataManagementUseCaseImpl(DotRepository repository) {
        this.repository = repository;
    }

    @Override
    public LiveData<Resource<List<DataValue>>> getLastValuesFromAllMainDashboard() {
        return repository.getLastValuesFromAllMainDashboard();
    }

    @Override
    public LiveData<Resource<DataValue>> findLastForSensorId(int id) {
        return repository.findLastValuesForSensorId(id);
    }

    @Override
    public LiveData<Resource<List<DataValue>>> findLastForSensorIds(int[] ids) {
        return repository.findLastValuesForSensorIds(ids);
    }

    @Override
    public LiveData<Resource<List<DataValue>>> getLastValuesForSensorId(int id) {
        return repository.getLastValuesForSensorId(id);
    }

    @Override
    public LiveData<Resource<List<DataValue>>> getAvgLastOneDayValuesForSensorId(int id) {
        return repository.getAvgLastOneDayValuesForSensorId(id);
    }

    @Override
    public LiveData<Resource<List<DataValue>>> getAvgLastOneWeekValuesForSensorId(int id) {
        return repository.getAvgLastOneWeekValuesForSensorId(id);
    }

    @Override
    public LiveData<Resource<List<DataValue>>> getAvgLastOneMonthValuesForSensorId(int id) {
        return repository.getAvgLastOneMonthValuesForSensorId(id);
    }

    @Override
    public LiveData<Resource<List<DataValue>>> getAvgLastOneYearValuesForSensorId(int id) {
        return repository.getAvgLastOneYearValuesForSensorId(id);
    }

    public LiveData<Resource> requestSensorReload(Sensor sensor) {
        return repository.requestSensorReload(sensor);
    }

    public LiveData<Resource> updateActuatorData(Actuator actuator, String data) {
        return repository.updateActuatorData(actuator, data);
    }
}
