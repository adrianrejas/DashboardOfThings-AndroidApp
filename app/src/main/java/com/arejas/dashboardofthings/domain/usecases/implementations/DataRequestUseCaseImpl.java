package com.arejas.dashboardofthings.domain.usecases.implementations;

import androidx.lifecycle.LiveData;

import com.arejas.dashboardofthings.data.interfaces.DotRepository;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.usecases.DataRequestUseCase;

import java.util.List;

public class DataRequestUseCaseImpl implements DataRequestUseCase {

    private final DotRepository repository;

    public DataRequestUseCaseImpl(DotRepository repository) {
        this.repository = repository;
    }

    @Override
    public LiveData<List<DataValue>> getLastFromAll() {
        return repository.getLastValuesFromAll();
    }

    @Override
    public LiveData<DataValue> findLastForSensorId(int id) {
        return repository.findLastValuesForSensorId(id);
    }

    @Override
    public LiveData<DataValue> findLastForSensorIds(int[] ids) {
        return repository.findLastValuesForSensorIds(ids);
    }

    @Override
    public LiveData<List<DataValue>> getLastValuesForSensorId(int id) {
        return repository.getLastValuesForSensorId(id);
    }

    @Override
    public LiveData<List<DataValue>> getAvgLastOneDayValuesForSensorId(int id) {
        return repository.getAvgLastOneDayValuesForSensorId(id);
    }

    @Override
    public LiveData<List<DataValue>> getAvgLastOneWeekValuesForSensorId(int id) {
        return repository.getAvgLastOneWeekValuesForSensorId(id);
    }

    @Override
    public LiveData<List<DataValue>> getAvgLastOneMonthValuesForSensorId(int id) {
        return repository.getAvgLastOneMonthValuesForSensorId(id);
    }

    @Override
    public LiveData<List<DataValue>> getAvgLastOneYearValuesForSensorId(int id) {
        return repository.getAvgLastOneYearValuesForSensorId(id);
    }
}
