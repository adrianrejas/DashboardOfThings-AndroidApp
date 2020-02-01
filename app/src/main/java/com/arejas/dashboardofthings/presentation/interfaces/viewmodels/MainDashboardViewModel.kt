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
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper

import java.util.HashMap

class MainDashboardViewModel(
    application: Application,
    private val sensorManagementUseCase: SensorManagementUseCase,
    private val actuatorManagementUseCase: ActuatorManagementUseCase,
    private val dataManagementUseCase: DataManagementUseCase,
    private val logsManagementUseCase: LogsManagementUseCase
) : AndroidViewModel(application) {

    private var sensorsInDashboard: LiveData<Resource<List<SensorExtended>>>? = null
    private var actuatorsInDashboard: LiveData<Resource<List<ActuatorExtended>>>? = null
    private var sensorNotificationsInDashboard: LiveData<Resource<List<Log>>>? = null
    private var logsInDashboard: LiveData<Resource<List<Log>>>? = null
    private var sensorsInDashboardLastValues: LiveData<Resource<List<DataValue>>>? = null

    private val mapSensorLastValuesLiveData: MutableMap<Int, LiveData<Resource<List<DataValue>>>>
    private val mapSensorLastHourLiveData: MutableMap<Int, LiveData<Resource<List<DataValue>>>>
    private val mapSensorLastWeekLiveData: MutableMap<Int, LiveData<Resource<List<DataValue>>>>
    private val mapSensorLastMonthLiveData: MutableMap<Int, LiveData<Resource<List<DataValue>>>>
    private val mapSensorLastYearLiveData: MutableMap<Int, LiveData<Resource<List<DataValue>>>>

    init {
        mapSensorLastValuesLiveData = HashMap()
        mapSensorLastHourLiveData = HashMap()
        mapSensorLastWeekLiveData = HashMap()
        mapSensorLastMonthLiveData = HashMap()
        mapSensorLastYearLiveData = HashMap()
    }

    fun getListOfSensorsMainDashboard(refreshData: Boolean): LiveData<Resource<List<SensorExtended>>>? {
        if (refreshData) sensorsInDashboard = null
        if (sensorsInDashboard == null) {
            sensorsInDashboard = this.sensorManagementUseCase.listOfSensorsMainDashboard
        }
        return sensorsInDashboard
    }

    fun requestSensorReload(sensor: Sensor) {
        val resultLiveData = this.dataManagementUseCase.requestSensorReload(sensor)
        val observer = object : Observer<Resource<*>> {
            override fun onChanged(result: Resource<*>?) {
                if (result != null) {
                    if (result.status == Resource.Status.LOADING) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_sensor_reload_request_loading))
                    } else if (result.status == Resource.Status.SUCCESS) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_sensor_reload_request_success))
                        resultLiveData.removeObserver(this)
                    } else if (result.status == Resource.Status.ERROR) {
                        ToastHelper.showToast(DotApplication.context.getString(R.string.toast_sensor_reload_request_failed))
                        resultLiveData.removeObserver(this)
                    }
                    return
                }
            }
        }
        resultLiveData.observeForever(observer)
    }

    fun getListOfActuatorsMainDashboard(refreshData: Boolean): LiveData<Resource<List<ActuatorExtended>>>? {
        if (refreshData) actuatorsInDashboard = null
        if (actuatorsInDashboard == null) {
            actuatorsInDashboard = this.actuatorManagementUseCase.listOfActuatorsMainDashboard
        }
        return actuatorsInDashboard
    }

    fun getListOfSensorsInDashboardLastValues(refreshData: Boolean): LiveData<Resource<List<DataValue>>>? {
        if (refreshData) sensorsInDashboardLastValues = null
        if (sensorsInDashboardLastValues == null) {
            sensorsInDashboardLastValues = this.dataManagementUseCase.lastValuesFromAllMainDashboard
        }
        return sensorsInDashboardLastValues
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

    fun getHistoricalValue(id: Int, position: Int): LiveData<Resource<List<DataValue>>>? {
        when (position) {
            1 -> return getAvgLastOneDayValuesForSensorId(id)
            2 -> return getAvgLastOneWeekValuesForSensorId(id)
            3 -> return getAvgLastOneMonthValuesForSensorId(id)
            4 -> return getAvgLastOneYearValuesForSensorId(id)
            else -> return getLastValuesForSensorId(id)
        }
    }

    fun getLastValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        var returningLiveData = mapSensorLastValuesLiveData[id]
        if (returningLiveData == null ||
            returningLiveData.value == null ||
            returningLiveData.value!!.status != Resource.Status.SUCCESS
        ) {
            returningLiveData = this.dataManagementUseCase.getLastValuesForSensorId(id)
            if (returningLiveData != null)
                mapSensorLastValuesLiveData[id] = returningLiveData
        }
        return returningLiveData
    }

    fun getAvgLastOneDayValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        var returningLiveData = mapSensorLastHourLiveData[id]
        if (returningLiveData == null ||
            returningLiveData.value == null ||
            returningLiveData.value!!.status != Resource.Status.SUCCESS
        ) {
            returningLiveData = this.dataManagementUseCase.getAvgLastOneDayValuesForSensorId(id)
            if (returningLiveData != null)
                mapSensorLastHourLiveData[id] = returningLiveData
        }
        return returningLiveData
    }

    fun getAvgLastOneWeekValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        var returningLiveData = mapSensorLastWeekLiveData[id]
        if (returningLiveData == null ||
            returningLiveData.value == null ||
            returningLiveData.value!!.status != Resource.Status.SUCCESS
        ) {
            returningLiveData = this.dataManagementUseCase.getAvgLastOneWeekValuesForSensorId(id)
            if (returningLiveData != null)
                mapSensorLastWeekLiveData[id] = returningLiveData
        }
        return returningLiveData
    }

    fun getAvgLastOneMonthValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        var returningLiveData = mapSensorLastMonthLiveData[id]
        if (returningLiveData == null ||
            returningLiveData.value == null ||
            returningLiveData.value!!.status != Resource.Status.SUCCESS
        ) {
            returningLiveData = this.dataManagementUseCase.getAvgLastOneMonthValuesForSensorId(id)
            if (returningLiveData != null)
                mapSensorLastMonthLiveData[id] = returningLiveData
        }
        return returningLiveData
    }

    fun getAvgLastOneYearValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        var returningLiveData = mapSensorLastYearLiveData[id]
        if (returningLiveData == null ||
            returningLiveData.value == null ||
            returningLiveData.value!!.status != Resource.Status.SUCCESS
        ) {
            returningLiveData = this.dataManagementUseCase.getAvgLastOneYearValuesForSensorId(id)
            if (returningLiveData != null)
                mapSensorLastYearLiveData[id] = returningLiveData
        }
        return returningLiveData
    }

    fun getLastConfigurationLogs(refreshData: Boolean): LiveData<Resource<List<Log>>>? {
        if (refreshData) logsInDashboard = null
        if (logsInDashboard == null) {
            logsInDashboard = this.logsManagementUseCase.lastConfigurationLogs
        }
        return logsInDashboard
    }

    fun getLastSensorNotificationLogsInMainDashboard(refreshData: Boolean): LiveData<Resource<List<Log>>>? {
        if (refreshData) sensorNotificationsInDashboard = null
        if (sensorNotificationsInDashboard == null) {
            sensorNotificationsInDashboard =
                this.logsManagementUseCase.lastNotificationLogsForSensorsInMainDashboard
        }
        return sensorNotificationsInDashboard
    }

}
