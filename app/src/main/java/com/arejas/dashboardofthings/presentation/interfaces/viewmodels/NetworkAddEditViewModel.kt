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

class NetworkAddEditViewModel(
    application: Application,
    private val networkManagementUseCase: NetworkManagementUseCase
) : AndroidViewModel(application) {

    private var networkId: Int? = null

    var networkBeingEdited: Network? = null

    private var network: LiveData<Resource<NetworkExtended>>? = null

    init {
        this.networkId = null
    }

    fun getNetworkId(): Int? {
        return networkId
    }

    fun setNetworkId(id: Int?): Int? {
        if (id == null || this.networkId !== id) {
            network = null
            networkBeingEdited = null
            network = null
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

    fun createNetwork(network: Network) {
        val resultLiveData = this.networkManagementUseCase.createNetwork(network)
        val observer = object : Observer<Resource<*>> {
            override fun onChanged(result: Resource<*>?) {
                if (result != null) {
                    if (result.status == Resource.Status.LOADING) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_creating))
                    } else if (result.status == Resource.Status.SUCCESS) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_create_succesful))
                        resultLiveData.removeObserver(this)
                    } else if (result.status == Resource.Status.ERROR) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_create_failed))
                        resultLiveData.removeObserver(this)
                    }
                    return
                }
            }
        }
        resultLiveData.observeForever(observer)
    }

    fun updateNetwork(network: Network) {
        val resultLiveData = this.networkManagementUseCase.updateNetwork(network)
        val observer = object : Observer<Resource<*>> {
            override fun onChanged(result: Resource<*>?) {
                if (result != null) {
                    if (result.status == Resource.Status.LOADING) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_updating))
                    } else if (result.status == Resource.Status.SUCCESS) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_update_succesful))
                        resultLiveData.removeObserver(this)
                    } else if (result.status == Resource.Status.ERROR) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_update_failed))
                        resultLiveData.removeObserver(this)
                    }
                    return
                }
            }
        }
        resultLiveData.observeForever(observer)
    }

}
