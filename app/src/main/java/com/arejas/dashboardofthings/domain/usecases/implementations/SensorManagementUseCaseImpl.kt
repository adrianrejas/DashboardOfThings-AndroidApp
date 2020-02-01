package com.arejas.dashboardofthings.domain.usecases.implementations

import androidx.lifecycle.LiveData

import com.arejas.dashboardofthings.data.interfaces.DotRepository
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.entities.widget.SensorWidgetItem
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase
import com.arejas.dashboardofthings.utils.functional.Consumer

class SensorManagementUseCaseImpl(private val repository: DotRepository) : SensorManagementUseCase {

    override val listOfSensors: LiveData<Resource<List<SensorExtended>>>?
        get() = repository.listOfSensors

    override val listOfSensorsMainDashboard: LiveData<Resource<List<SensorExtended>>>?
        get() = repository.listOfSensorsMainDashboard

    override val listOfSensorsLocated: LiveData<Resource<List<SensorExtended>>>?
        get() = repository.listOfSensorsLocated

    override fun getSensor(sensorId: Int): LiveData<Resource<SensorExtended>>? {
        return repository.getSensor(sensorId)
    }

    override fun createSensor(sensor: Sensor): LiveData<Resource<*>> {
        return repository.createSensor(sensor)
    }

    override fun updateSensor(sensor: Sensor): LiveData<Resource<*>> {
        return repository.updateSensor(sensor)
    }

    override fun deleteSensor(sensor: Sensor): LiveData<Resource<*>> {
        return repository.deleteSensor(sensor)
    }

    override fun getSensorInfoForWidget(id: Int, consumer: Consumer<SensorWidgetItem>) {
        repository.getSensorInfoForWidget(id, consumer)
    }

    override fun getSensorInfoForWidgets(
        ids: IntArray,
        consumer: Consumer<List<SensorWidgetItem>>
    ) {
        repository.getSensorInfoForWidgets(ids, consumer)
    }

    override fun requestSensorReload(id: Int) {
        repository.requestSensorReloadInstant(id)
    }
}
