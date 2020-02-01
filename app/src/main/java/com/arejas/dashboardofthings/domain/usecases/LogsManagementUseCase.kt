package com.arejas.dashboardofthings.domain.usecases

import androidx.lifecycle.LiveData

import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.database.Log
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.utils.Enumerators

interface LogsManagementUseCase : BaseUseCase {

    val lastConfigurationLogs: LiveData<Resource<List<Log>>>

    val lastNotificationLogsForSensorsInMainDashboard: LiveData<Resource<List<Log>>>

    fun getLastLogsForNetwork(networkId: Int): LiveData<Resource<List<Log>>>

    fun getLastLogsForSensor(sensorId: Int): LiveData<Resource<List<Log>>>

    fun getLastLogsForActuator(actuatorId: Int): LiveData<Resource<List<Log>>>

    fun getLastNotificationLogForElement(
        elementId: Int,
        elementType: Enumerators.ElementType
    ): LiveData<Log>

}
