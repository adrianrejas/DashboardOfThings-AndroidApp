package com.arejas.dashboardofthings.domain.usecases.implementations;

import androidx.lifecycle.LiveData;

import com.arejas.dashboardofthings.data.interfaces.DotRepository;
import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.domain.usecases.LogsRequestUseCase;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.List;

public class LogsRequestUseCaseImpl implements LogsRequestUseCase {

    private final DotRepository repository;

    public LogsRequestUseCaseImpl(DotRepository repository) {
        this.repository = repository;
    }

    @Override
    public LiveData<List<Log>> getLastLogs() {
        return repository.getLastLogs();
    }

    @Override
    public LiveData<List<Log>> getLastLogsForNetwork(int networkId) {
        return repository.getLastLogsForElement(networkId, Enumerators.ElementType.NETWORK);
    }

    @Override
    public LiveData<List<Log>> getLastLogsForSensor(int sensorId) {
        return repository.getLastLogsForElement(sensorId, Enumerators.ElementType.SENSOR);
    }

    @Override
    public LiveData<List<Log>> getLastLogsForActuator(int actuatorId) {
        return repository.getLastLogsForElement(actuatorId, Enumerators.ElementType.ACTUATOR);
    }
}
