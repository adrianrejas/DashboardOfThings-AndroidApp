package com.arejas.dashboardofthings.domain.usecases.implementations

import androidx.lifecycle.LiveData

import com.arejas.dashboardofthings.data.interfaces.DotRepository
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase

class ActuatorManagementUseCaseImpl(private val repository: DotRepository) :
    ActuatorManagementUseCase {

    override val listOfActuators: LiveData<Resource<List<ActuatorExtended>>>?
        get() = repository.listOfActuators

    override val listOfActuatorsMainDashboard: LiveData<Resource<List<ActuatorExtended>>>?
        get() = repository.listOfActuatorsMainDashboard

    override val listOfActuatorsLocated: LiveData<Resource<List<ActuatorExtended>>>?
        get() = repository.listOfActuatorsLocated

    override fun getActuator(actuatorId: Int): LiveData<Resource<ActuatorExtended>>? {
        return repository.getActuator(actuatorId)
    }

    override fun createActuator(actuator: Actuator): LiveData<Resource<*>> {
        return repository.createActuator(actuator)
    }

    override fun updateActuator(actuator: Actuator): LiveData<Resource<*>> {
        return repository.updateActuator(actuator)
    }

    override fun deleteActuator(actuator: Actuator): LiveData<Resource<*>> {
        return repository.deleteActuator(actuator)
    }
}
