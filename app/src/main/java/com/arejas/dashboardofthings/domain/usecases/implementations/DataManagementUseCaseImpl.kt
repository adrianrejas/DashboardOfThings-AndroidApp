package com.arejas.dashboardofthings.domain.usecases.implementations

import androidx.lifecycle.LiveData

import com.arejas.dashboardofthings.data.interfaces.DotRepository
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase

class DataManagementUseCaseImpl(private val repository: DotRepository) : DataManagementUseCase {

    override val lastValuesFromAllMainDashboard: LiveData<Resource<List<DataValue>>>?
        get() = repository.lastValuesFromAllMainDashboard

    override fun findLastForSensorId(id: Int): LiveData<Resource<DataValue>>? {
        return repository.findLastValuesForSensorId(id)
    }

    override fun findLastForSensorIds(ids: IntArray): LiveData<Resource<List<DataValue>>>? {
        return repository.findLastValuesForSensorIds(ids)
    }

    override fun getLastValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        return repository.getLastValuesForSensorId(id)
    }

    override fun getAvgLastOneDayValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        return repository.getAvgLastOneDayValuesForSensorId(id)
    }

    override fun getAvgLastOneWeekValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        return repository.getAvgLastOneWeekValuesForSensorId(id)
    }

    override fun getAvgLastOneMonthValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        return repository.getAvgLastOneMonthValuesForSensorId(id)
    }

    override fun getAvgLastOneYearValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        return repository.getAvgLastOneYearValuesForSensorId(id)
    }

    override fun requestSensorReload(sensor: Sensor): LiveData<Resource<*>> {
        return repository.requestSensorReload(sensor)
    }

    override fun updateActuatorData(actuator: Actuator, data: String): LiveData<Resource<*>> {
        return repository.updateActuatorData(actuator, data)
    }
}
