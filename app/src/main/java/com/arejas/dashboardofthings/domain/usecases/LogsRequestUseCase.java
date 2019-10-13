package com.arejas.dashboardofthings.domain.usecases;

import androidx.lifecycle.LiveData;

import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.List;

public interface LogsRequestUseCase extends BaseUseCase {

    public LiveData<List<Log>> getLastLogs();

    public LiveData<List<Log>> getLastLogsForNetwork(int networkId);

    public LiveData<List<Log>> getLastLogsForSensor(int sensorId);

    public LiveData<List<Log>> getLastLogsForActuator(int actuatorId);

}
