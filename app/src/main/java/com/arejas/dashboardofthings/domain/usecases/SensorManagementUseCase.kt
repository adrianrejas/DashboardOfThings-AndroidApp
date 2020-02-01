package com.arejas.dashboardofthings.domain.usecases

import androidx.lifecycle.LiveData

import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.LiveDataResource
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.entities.widget.SensorWidgetItem
import com.arejas.dashboardofthings.utils.functional.Consumer

interface SensorManagementUseCase : BaseUseCase {

    val listOfSensors: LiveData<Resource<List<SensorExtended>>>

    val listOfSensorsMainDashboard: LiveData<Resource<List<SensorExtended>>>

    val listOfSensorsLocated: LiveData<Resource<List<SensorExtended>>>

    fun getSensor(sensorId: Int): LiveData<Resource<SensorExtended>>

    fun createSensor(sensor: Sensor): LiveData<Resource<*>>

    fun updateSensor(sensor: Sensor): LiveData<Resource<*>>

    fun deleteSensor(sensor: Sensor): LiveData<Resource<*>>

    fun getSensorInfoForWidget(id: Int, consumer: Consumer<SensorWidgetItem>)

    fun getSensorInfoForWidgets(ids: IntArray, consumer: Consumer<List<SensorWidgetItem>>)

    fun requestSensorReload(id: Int)

}
