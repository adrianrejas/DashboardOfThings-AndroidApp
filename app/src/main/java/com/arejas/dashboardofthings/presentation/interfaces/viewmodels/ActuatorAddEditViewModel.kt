package com.arejas.dashboardofthings.presentation.interfaces.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.usecases.NetworkManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper

class ActuatorAddEditViewModel(
    application: Application,
    private val networkManagementUseCase: NetworkManagementUseCase,
    private val actuatorManagementUseCase: ActuatorManagementUseCase
) : AndroidViewModel(application) {

    var actuatorId: Int? = null

    var actuatorBeingEdited: ActuatorExtended? = null

    private var actuator: LiveData<Resource<ActuatorExtended>>? = null
    private var networks: LiveData<Resource<List<NetworkExtended>>>? = null

    init {
        this.actuatorId = null
    }

    fun setActuatorId(id: Int?): Int? {
        if (id == null || this.actuatorId !== id) {
            actuator = null
            actuatorBeingEdited = null
            actuator = null
            this.actuatorId = id
            networks = null
        }
        return this.actuatorId
    }

    fun getActuator(refreshData: Boolean): LiveData<Resource<ActuatorExtended>>? {
        if (refreshData) actuator = null
        if (actuatorId == null) return null
        if (actuator == null) {
            actuator = this.actuatorManagementUseCase.getActuator(actuatorId!!)
        }
        return actuator
    }

    fun getNetworks(refreshData: Boolean): LiveData<Resource<List<NetworkExtended>>>? {
        if (refreshData) networks = null
        if (networks == null) {
            networks = this.networkManagementUseCase.listOfNetworks
        }
        return networks
    }

    fun createActuator(actuator: Actuator) {
        val resultLiveData = this.actuatorManagementUseCase.createActuator(actuator)
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

    fun updateActuator(actuator: Actuator) {
        val resultLiveData = this.actuatorManagementUseCase.updateActuator(actuator)
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
