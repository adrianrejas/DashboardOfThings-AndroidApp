package com.arejas.dashboardofthings.domain.usecases;

import androidx.lifecycle.LiveData;


import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.result.Resource;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DataManagementUseCase extends BaseUseCase {

    public LiveData<Resource<List<DataValue>>> getLastValuesFromAllMainDashboard();

    public LiveData<DataValue> findLastForSensorId(int id);

    public LiveData<DataValue> findLastForSensorIds(int[] ids);

    public LiveData<Resource<List<DataValue>>> getLastValuesForSensorId(int id);

    public LiveData<Resource<List<DataValue>>> getAvgLastOneDayValuesForSensorId(int id);

    public LiveData<Resource<List<DataValue>>> getAvgLastOneWeekValuesForSensorId(int id);

    public LiveData<Resource<List<DataValue>>> getAvgLastOneMonthValuesForSensorId(int id);

    public LiveData<Resource<List<DataValue>>> getAvgLastOneYearValuesForSensorId(int id);

    public LiveData<Resource> requestSensorReload(Sensor sensor);

    public LiveData<Resource> updateActuatorData(Actuator actuator, String data);

}
