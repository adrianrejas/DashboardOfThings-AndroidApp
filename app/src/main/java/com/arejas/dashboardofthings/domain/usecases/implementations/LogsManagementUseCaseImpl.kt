package com.arejas.dashboardofthings.domain.usecases.implementations

import androidx.lifecycle.LiveData

import com.arejas.dashboardofthings.data.interfaces.DotRepository
import com.arejas.dashboardofthings.domain.entities.database.Log
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase
import com.arejas.dashboardofthings.utils.Enumerators

class LogsManagementUseCaseImpl(private val repository: DotRepository) : LogsManagementUseCase {

    override val lastConfigurationLogs: LiveData<Resource<List<Log>>>?
        get() = repository.lastConfigurationLogs

    override val lastNotificationLogsForSensorsInMainDashboard: LiveData<Resource<List<Log>>>?
        get() = repository.lastNotificationLogsInMainDashboard

    override fun getLastLogsForNetwork(networkId: Int): LiveData<Resource<List<Log>>>? {
        return repository.getLastLogsForElement(networkId, Enumerators.ElementType.NETWORK)
    }

    override fun getLastLogsForSensor(sensorId: Int): LiveData<Resource<List<Log>>>? {
        return repository.getLastLogsForElement(sensorId, Enumerators.ElementType.SENSOR)
    }

    override fun getLastLogsForActuator(actuatorId: Int): LiveData<Resource<List<Log>>>? {
        return repository.getLastLogsForElement(actuatorId, Enumerators.ElementType.ACTUATOR)
    }

    override fun getLastNotificationLogForElement(
        elementId: Int,
        elementType: Enumerators.ElementType
    ): LiveData<Log>? {
        return repository.getLastNotificationLogForElement(elementId, elementType)
    }

}
