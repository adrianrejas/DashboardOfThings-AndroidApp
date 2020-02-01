package com.arejas.dashboardofthings.domain.usecases.implementations

import androidx.lifecycle.LiveData

import com.arejas.dashboardofthings.data.interfaces.DotRepository
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.usecases.NetworkManagementUseCase

class NetworkManagementUseCaseImpl(private val repository: DotRepository) :
    NetworkManagementUseCase {

    override val listOfNetworks: LiveData<Resource<List<NetworkExtended>>>?
        get() = repository.listOfNetworks

    override fun getNetwork(networkId: Int): LiveData<Resource<NetworkExtended>> {
        return repository.getNetwork(networkId)
    }

    override fun getListOfRelatedSensors(networkId: Int): LiveData<Resource<List<Sensor>>>? {
        return repository.getListOfSensorsFromSameNetwork(networkId)
    }

    override fun getListOfRelatedActuators(networkId: Int): LiveData<Resource<List<Actuator>>>? {
        return repository.getListOfActuatorsFromSameNetwork(networkId)
    }

    override fun createNetwork(network: Network): LiveData<Resource<*>> {
        return repository.createNetwork(network)
    }

    override fun updateNetwork(network: Network): LiveData<Resource<*>> {
        return repository.updateNetwork(network)
    }

    override fun deleteNetwork(network: Network): LiveData<Resource<*>> {
        return repository.deleteNetwork(network)
    }
}
