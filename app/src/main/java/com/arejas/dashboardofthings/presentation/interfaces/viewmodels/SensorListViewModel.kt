package com.arejas.dashboardofthings.presentation.interfaces.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper

class SensorListViewModel(
    application: Application,
    private val sensorManagementUseCase: SensorManagementUseCase
) : AndroidViewModel(application) {

    private var sensors: LiveData<Resource<List<SensorExtended>>>? = null

    fun getListOfSensors(refreshData: Boolean): LiveData<Resource<List<SensorExtended>>>? {
        if (refreshData) sensors = null
        if (sensors == null) {
            sensors = this.sensorManagementUseCase.listOfSensors
        }
        return sensors
    }

    fun removeSensor(sensor: Sensor) {
        val resultLiveData = this.sensorManagementUseCase.deleteSensor(sensor)
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
