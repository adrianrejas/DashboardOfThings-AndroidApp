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

class MapViewModel(
    application: Application,
    private val sensorManagementUseCase: SensorManagementUseCase,
    private val actuatorManagementUseCase: ActuatorManagementUseCase
) : AndroidViewModel(application) {

    private var sensorsLocated: LiveData<Resource<List<SensorExtended>>>? = null
    private var actuatorsLocated: LiveData<Resource<List<ActuatorExtended>>>? = null

    fun getListOfSensorsLocated(refreshData: Boolean): LiveData<Resource<List<SensorExtended>>>? {
        if (refreshData) sensorsLocated = null
        if (sensorsLocated == null) {
            sensorsLocated = this.sensorManagementUseCase.listOfSensorsLocated
        }
        return sensorsLocated
    }

    fun getListOfActuatorsLocated(refreshData: Boolean): LiveData<Resource<List<ActuatorExtended>>>? {
        if (refreshData) actuatorsLocated = null
        if (actuatorsLocated == null) {
            actuatorsLocated = this.actuatorManagementUseCase.listOfActuatorsLocated
        }
        return actuatorsLocated
    }

}
