package com.arejas.dashboardofthings.domain.usecases;

import androidx.lifecycle.LiveData;


import com.arejas.dashboardofthings.domain.entities.database.DataValue;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DataRequestUseCase extends BaseUseCase {

    public LiveData<List<DataValue>> getLastFromAll();

    public LiveData<DataValue> findLastForSensorId(int id);

    public LiveData<DataValue> findLastForSensorIds(int[] ids);

    public LiveData<List<DataValue>> getLastValuesForSensorId(int id);

    public LiveData<List<DataValue>> getAvgLastOneDayValuesForSensorId(int id);

    public LiveData<List<DataValue>> getAvgLastOneWeekValuesForSensorId(int id);

    public LiveData<List<DataValue>> getAvgLastOneMonthValuesForSensorId(int id);

    public LiveData<List<DataValue>> getAvgLastOneYearValuesForSensorId(int id);

}
