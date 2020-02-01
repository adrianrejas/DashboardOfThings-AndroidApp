package com.arejas.dashboardofthings.presentation.interfaces.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.Log
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.NetworkManagementUseCase
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper

class NetworkDetailsViewModel(
    application: Application,
    private val networkManagementUseCase: NetworkManagementUseCase,
    private val logsManagementUseCase: LogsManagementUseCase
) : AndroidViewModel(application) {

    private var networkId: Int? = null

    private var network: LiveData<Resource<NetworkExtended>>? = null
    private var sensorsRelated: LiveData<Resource<List<Sensor>>>? = null
    private var actuatorsRelated: LiveData<Resource<List<Actuator>>>? = null
    private var logs: LiveData<Resource<List<Log>>>? = null

    init {
        this.networkId = null
    }

    fun getNetworkId(): Int? {
        return networkId
    }

    fun setNetworkId(id: Int?): Int? {
        if (this.networkId !== id) {
            network = null
            sensorsRelated = null
            actuatorsRelated = null
            logs = null
            this.networkId = id
        }
        return this.networkId
    }

    fun getNetwork(refreshData: Boolean): LiveData<Resource<NetworkExtended>>? {
        if (refreshData) network = null
        if (networkId == null) return null
        if (network == null) {
            network = this.networkManagementUseCase.getNetwork(networkId!!)
        }
        return network
    }

    fun getSensorsRelated(refreshData: Boolean): LiveData<Resource<List<Sensor>>>? {
        if (refreshData) sensorsRelated = null
        if (networkId == null) return null
        if (sensorsRelated == null) {
            sensorsRelated = this.networkManagementUseCase.getListOfRelatedSensors(networkId!!)
        }
        return sensorsRelated
    }

    fun getActuatorsRelated(refreshData: Boolean): LiveData<Resource<List<Actuator>>>? {
        if (refreshData) actuatorsRelated = null
        if (networkId == null) return null
        if (actuatorsRelated == null) {
            actuatorsRelated = this.networkManagementUseCase.getListOfRelatedActuators(networkId!!)
        }
        return actuatorsRelated
    }

    fun getLogsForNetwork(refreshData: Boolean): LiveData<Resource<List<Log>>>? {
        if (refreshData) logs = null
        if (networkId == null) return null
        if (logs == null) {
            logs = this.logsManagementUseCase.getLastLogsForNetwork(networkId!!)
        }
        return logs
    }

    fun removeNetwork(network: Network) {
        val resultLiveData = this.networkManagementUseCase.deleteNetwork(network)
        val observer = object : Observer<Resource<*>> {
            override fun onChanged(result: Resource<*>?) {
                if (result != null) {
                    if (result.status == Resource.Status.LOADING) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_removing))
                    } else if (result.status == Resource.Status.SUCCESS) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_remove_succesful))
                        resultLiveData.removeObserver(this)
                    } else if (result.status == Resource.Status.ERROR) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_remove_failed))
                        resultLiveData.removeObserver(this)
                    }
                    return
                }
            }
        }
        resultLiveData.observeForever(observer)
    }

}
