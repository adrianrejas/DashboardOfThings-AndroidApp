package com.arejas.dashboardofthings.presentation.interfaces.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.database.Log
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.NetworkManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper

import java.util.HashMap

class NetworkListViewModel(
    application: Application,
    private val networkManagementUseCase: NetworkManagementUseCase
) : AndroidViewModel(application) {

    private var networks: LiveData<Resource<List<NetworkExtended>>>? = null

    fun getListOfNetworks(refreshData: Boolean): LiveData<Resource<List<NetworkExtended>>>? {
        if (refreshData) networks = null
        if (networks == null) {
            networks = this.networkManagementUseCase.listOfNetworks
        }
        return networks
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
