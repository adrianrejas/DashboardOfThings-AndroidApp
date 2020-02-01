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
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper

class SensorDetailsViewModel(
    application: Application,
    private val sensorManagementUseCase: SensorManagementUseCase,
    private val dataManagementUseCase: DataManagementUseCase,
    private val logsManagementUseCase: LogsManagementUseCase
) : AndroidViewModel(application) {

    var historySpinnerPosition: Int = 0

    private var sensorId: Int? = null

    private var sensor: LiveData<Resource<SensorExtended>>? = null
    private var logs: LiveData<Resource<List<Log>>>? = null
    private var lastValue: LiveData<Resource<DataValue>>? = null

    private var sensorLastValuesLiveData: LiveData<Resource<List<DataValue>>>? = null
    private var sensorLastHourLiveData: LiveData<Resource<List<DataValue>>>? = null
    private var sensorLastWeekLiveData: LiveData<Resource<List<DataValue>>>? = null
    private var sensorLastMonthLiveData: LiveData<Resource<List<DataValue>>>? = null
    private var sensorLastYearLiveData: LiveData<Resource<List<DataValue>>>? = null

    init {
        this.historySpinnerPosition = 0
        this.sensorId = null
    }

    fun getSensorId(): Int? {
        return sensorId
    }

    fun setSensorId(id: Int?): Int? {
        if (this.sensorId !== id) {
            this.sensorId = id
            sensor = null
            logs = null
            lastValue = null
            sensorLastValuesLiveData = null
            sensorLastHourLiveData = null
            sensorLastWeekLiveData = null
            sensorLastMonthLiveData = null
            sensorLastYearLiveData = null
            historySpinnerPosition = 0
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

    fun getLastValueForSensor(refreshData: Boolean): LiveData<Resource<DataValue>>? {
        if (refreshData) lastValue = null
        if (sensorId == null) return null
        if (lastValue == null) {
            lastValue = this.dataManagementUseCase.findLastForSensorId(sensorId!!)
        }
        return lastValue
    }

    fun getLogsForSensor(refreshData: Boolean): LiveData<Resource<List<Log>>>? {
        if (refreshData) logs = null
        if (sensorId == null) return null
        if (logs == null) {
            logs = this.logsManagementUseCase.getLastLogsForSensor(sensorId!!)
        }
        return logs
    }

    fun requestSensorReload(): LiveData<Resource<*>>? {
        sensor?.value?.let { resource -> resource.status.equals(Resource.Status.SUCCESS).let {
            resource.data?.let { sensorExtended ->
                return this.dataManagementUseCase.requestSensorReload(sensorExtended)
            }
        } }
        return null
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

    fun getHistoricalData(refreshData: Boolean): LiveData<Resource<List<DataValue>>>? {
        when (historySpinnerPosition) {
            1 -> return getAvgLastOneDayValuesForSensor(refreshData)
            2 -> return getAvgLastOneWeekValuesForSensor(refreshData)
            3 -> return getAvgLastOneMonthValuesForSensor(refreshData)
            4 -> return getAvgLastOneYearValuesForSensor(refreshData)
            else -> return getLastValuesForSensor(refreshData)
        }
    }

    fun getLastValuesForSensor(refreshData: Boolean): LiveData<Resource<List<DataValue>>>? {
        if (refreshData) sensorLastValuesLiveData = null
        if (sensorId == null) return null
        if (sensorLastValuesLiveData == null) {
            sensorLastValuesLiveData =
                this.dataManagementUseCase.getLastValuesForSensorId(sensorId!!)
        }
        return sensorLastValuesLiveData
    }

    fun getAvgLastOneDayValuesForSensor(refreshData: Boolean): LiveData<Resource<List<DataValue>>>? {
        if (refreshData) sensorLastHourLiveData = null
        if (sensorId == null) return null
        if (sensorLastHourLiveData == null) {
            sensorLastHourLiveData =
                this.dataManagementUseCase.getAvgLastOneDayValuesForSensorId(sensorId!!)
        }
        return sensorLastHourLiveData
    }

    fun getAvgLastOneWeekValuesForSensor(refreshData: Boolean): LiveData<Resource<List<DataValue>>>? {
        if (refreshData) sensorLastWeekLiveData = null
        if (sensorId == null) return null
        if (sensorLastWeekLiveData == null) {
            sensorLastWeekLiveData =
                this.dataManagementUseCase.getAvgLastOneWeekValuesForSensorId(sensorId!!)
        }
        return sensorLastWeekLiveData
    }

    fun getAvgLastOneMonthValuesForSensor(refreshData: Boolean): LiveData<Resource<List<DataValue>>>? {
        if (refreshData) sensorLastMonthLiveData = null
        if (sensorId == null) return null
        if (sensorLastMonthLiveData == null) {
            sensorLastMonthLiveData =
                this.dataManagementUseCase.getAvgLastOneMonthValuesForSensorId(sensorId!!)
        }
        return sensorLastMonthLiveData
    }

    fun getAvgLastOneYearValuesForSensor(refreshData: Boolean): LiveData<Resource<List<DataValue>>>? {
        if (refreshData) sensorLastYearLiveData = null
        if (sensorId == null) return null
        if (sensorLastYearLiveData == null) {
            sensorLastYearLiveData =
                this.dataManagementUseCase.getAvgLastOneYearValuesForSensorId(sensorId!!)
        }
        return sensorLastYearLiveData
    }

}
