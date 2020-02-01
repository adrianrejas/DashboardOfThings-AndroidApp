package com.arejas.dashboardofthings.domain.usecases

import androidx.lifecycle.LiveData

import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource

interface ActuatorManagementUseCase : BaseUseCase {

    val listOfActuators: LiveData<Resource<List<ActuatorExtended>>>

    val listOfActuatorsMainDashboard: LiveData<Resource<List<ActuatorExtended>>>

    val listOfActuatorsLocated: LiveData<Resource<List<ActuatorExtended>>>

    fun getActuator(actuatorId: Int): LiveData<Resource<ActuatorExtended>>

    fun createActuator(actuator: Actuator): LiveData<Resource<*>>

    fun updateActuator(actuator: Actuator): LiveData<Resource<*>>

    fun deleteActuator(actuator: Actuator): LiveData<Resource<*>>

}
