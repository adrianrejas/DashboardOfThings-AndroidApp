package com.arejas.dashboardofthings.domain.usecases

import androidx.lifecycle.LiveData


import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.result.Resource

interface DataManagementUseCase : BaseUseCase {

    val lastValuesFromAllMainDashboard: LiveData<Resource<List<DataValue>>>

    fun findLastForSensorId(id: Int): LiveData<Resource<DataValue>>

    fun findLastForSensorIds(ids: IntArray): LiveData<Resource<List<DataValue>>>

    fun getLastValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>

    fun getAvgLastOneDayValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>

    fun getAvgLastOneWeekValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>

    fun getAvgLastOneMonthValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>

    fun getAvgLastOneYearValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>

    fun requestSensorReload(sensor: Sensor): LiveData<Resource<*>>

    fun updateActuatorData(actuator: Actuator, data: String): LiveData<Resource<*>>

}
