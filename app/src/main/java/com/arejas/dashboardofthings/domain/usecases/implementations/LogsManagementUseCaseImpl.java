package com.arejas.dashboardofthings.domain.usecases.implementations;

import androidx.lifecycle.LiveData;

import com.arejas.dashboardofthings.data.interfaces.DotRepository;
import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.List;

public class LogsManagementUseCaseImpl implements LogsManagementUseCase {

    private final DotRepository repository;

    public LogsManagementUseCaseImpl(DotRepository repository) {
        this.repository = repository;
    }

    @Override
    public LiveData<Resource<List<Log>>> getLastLogsForNetwork(int networkId) {
        return repository.getLastLogsForElement(networkId, Enumerators.ElementType.NETWORK);
    }

    @Override
    public LiveData<Resource<List<Log>>> getLastLogsForSensor(int sensorId) {
        return repository.getLastLogsForElement(sensorId, Enumerators.ElementType.SENSOR);
    }

    @Override
    public LiveData<Resource<List<Log>>> getLastLogsForActuator(int actuatorId) {
        return repository.getLastLogsForElement(actuatorId, Enumerators.ElementType.ACTUATOR);
    }

    @Override
    public LiveData<Resource<List<Log>>> getLastConfigurationLogs() {
        return repository.getLastConfigurationLogs();
    }

    @Override
    public LiveData<Resource<List<Log>>> getLastNotificationLogsForSensorsInMainDashboard() {
        return repository.getLastNotificationLogsInMainDashboard();
    }

    @Override
    public LiveData<Log> getLastNotificationLogForElement(int elementId,
                                                          Enumerators.ElementType elementType) {
        return repository.getLastNotificationLogForElement(elementId, elementType);
    }

}
