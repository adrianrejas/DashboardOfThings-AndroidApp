package com.arejas.dashboardofthings.presentation.interfaces.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.database.Log
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper

class ActuatorDetailsViewModel(
    application: Application,
    private val actuatorManagementUseCase: ActuatorManagementUseCase,
    private val dataManagementUseCase: DataManagementUseCase,
    private val logsManagementUseCase: LogsManagementUseCase
) : AndroidViewModel(application) {

    var historySpinnerPosition: Int = 0

    var actuatorId: Int? = null

    private var actuator: LiveData<Resource<ActuatorExtended>>? = null
    private var logs: LiveData<Resource<List<Log>>>? = null

    init {
        this.historySpinnerPosition = 0
        this.actuatorId = null
    }

    fun setActuatorId(id: Int?): Int? {
        if (this.actuatorId !== id) {
            this.actuatorId = id
            actuator = null
            logs = null
            historySpinnerPosition = 0
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

    fun getLogsForActuator(refreshData: Boolean): LiveData<Resource<List<Log>>>? {
        if (refreshData) logs = null
        if (actuatorId == null) return null
        if (logs == null) {
            logs = this.logsManagementUseCase.getLastLogsForActuator(actuatorId!!)
        }
        return logs
    }

    fun sendActuatorData(actuator: Actuator, data: String) {
        val resultLiveData = this.dataManagementUseCase.updateActuatorData(actuator, data)
        val observer = object : Observer<Resource<*>> {
            override fun onChanged(result: Resource<*>?) {
                if (result != null) {
                    if (result.status == Resource.Status.LOADING) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_actuator_send_loading))
                    } else if (result.status == Resource.Status.SUCCESS) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_actuator_send_success))
                        resultLiveData.removeObserver(this)
                    } else if (result.status == Resource.Status.ERROR) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_actuator_send_failed))
                        resultLiveData.removeObserver(this)
                    }
                    return
                }
            }
        }
        resultLiveData.observeForever(observer)
    }

    fun sendActuatorData(data: String) {
        actuator?.value?.let { resource ->
            resource.status.equals( Resource.Status.SUCCESS).let {
                resource.data?.let { actuatorExtended -> sendActuatorData(actuatorExtended, data) }
            }
        }
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
