package com.arejas.dashboardofthings.domain.usecases

import androidx.lifecycle.LiveData

import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource

interface NetworkManagementUseCase : BaseUseCase {

    val listOfNetworks: LiveData<Resource<List<NetworkExtended>>>

    fun getNetwork(networkId: Int): LiveData<Resource<NetworkExtended>>

    fun getListOfRelatedSensors(networkId: Int): LiveData<Resource<List<Sensor>>>

    fun getListOfRelatedActuators(networkId: Int): LiveData<Resource<List<Actuator>>>

    fun createNetwork(network: Network): LiveData<Resource<*>>

    fun updateNetwork(network: Network): LiveData<Resource<*>>

    fun deleteNetwork(network: Network): LiveData<Resource<*>>

}
