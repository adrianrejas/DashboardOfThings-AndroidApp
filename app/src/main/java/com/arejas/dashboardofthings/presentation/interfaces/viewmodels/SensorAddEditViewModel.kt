package com.arejas.dashboardofthings.presentation.interfaces.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.usecases.NetworkManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper

class SensorAddEditViewModel(
    application: Application,
    private val networkManagementUseCase: NetworkManagementUseCase,
    private val sensorManagementUseCase: SensorManagementUseCase
) : AndroidViewModel(application) {

    private var sensorId: Int? = null

    var sensorBeingEdited: SensorExtended? = null

    private var sensor: LiveData<Resource<SensorExtended>>? = null
    private var networks: LiveData<Resource<List<NetworkExtended>>>? = null

    init {
        this.sensorId = null
    }

    fun getSensorId(): Int? {
        return sensorId
    }

    fun setSensorId(id: Int?): Int? {
        if (id == null || this.sensorId !== id) {
            sensor = null
            sensorBeingEdited = null
            sensor = null
            this.sensorId = id
            networks = null
        }
        return this.sensorId
    }

    fun getSensor(refreshData: Boolean): LiveData<Resource<SensorExtended>>? {
        if (refreshData) sensor = null
        if (sensorId == null) return null
        if (sensor == null) {
            sensor = this.sensorManagementUseCase.getSensor(sensorId!!)
        }
        return sensor
    }

    fun getNetworks(refreshData: Boolean): LiveData<Resource<List<NetworkExtended>>>? {
        if (refreshData) networks = null
        if (networks == null) {
            networks = this.networkManagementUseCase.listOfNetworks
        }
        return networks
    }

    fun createSensor(sensor: Sensor) {
        val resultLiveData = this.sensorManagementUseCase.createSensor(sensor)
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

    fun updateSensor(sensor: Sensor) {
        val resultLiveData = this.sensorManagementUseCase.updateSensor(sensor)
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
