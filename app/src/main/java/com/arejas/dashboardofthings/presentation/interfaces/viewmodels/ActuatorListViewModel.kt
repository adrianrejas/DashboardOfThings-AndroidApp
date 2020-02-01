package com.arejas.dashboardofthings.presentation.interfaces.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper

class ActuatorListViewModel(
    application: Application,
    private val actuatorManagementUseCase: ActuatorManagementUseCase
) : AndroidViewModel(application) {

    private var actuators: LiveData<Resource<List<ActuatorExtended>>>? = null

    fun getListOfActuators(refreshData: Boolean): LiveData<Resource<List<ActuatorExtended>>>? {
        if (refreshData) actuators = null
        if (actuators == null) {
            actuators = this.actuatorManagementUseCase.listOfActuators
        }
        return actuators
    }

    fun removeActuator(actuator: Actuator) {
        val resultLiveData = this.actuatorManagementUseCase.deleteActuator(actuator)
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
