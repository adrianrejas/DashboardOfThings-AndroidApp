package com.arejas.dashboardofthings.data.interfaces

import android.content.Context
import android.util.Pair

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.data.helpers.DataHelper
import com.arejas.dashboardofthings.data.sources.database.DotDatabase
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.database.Log
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.LiveDataResource
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.entities.widget.SensorWidgetItem
import com.arejas.dashboardofthings.presentation.ui.notifications.NotificationsHelper
import com.arejas.dashboardofthings.presentation.ui.widget.SensorWidget
import com.arejas.dashboardofthings.utils.Enumerators
import com.arejas.dashboardofthings.utils.rx.RxHelper
import io.reactivex.functions.Consumer

import java.util.Calendar
import java.util.Date
import java.util.Random
import java.util.concurrent.Executor

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class DotRepository @Inject
constructor(
    internal var dotDatabase: DotDatabase,
    @param:Named("dbExecutorManagement") internal var dbExecutorManagement: Executor,
    @param:Named("dbExecutorDataInsert") internal var dbExecutorDataInsert: Executor,
    internal var appContext: Context
) {

    internal var networkList: LiveData<Resource<List<NetworkExtended>>>? = null
    internal var sensorList: LiveData<Resource<List<SensorExtended>>>? = null
    internal var sensorListMainDashboard: LiveData<Resource<List<SensorExtended>>>? = null
    internal var sensorListLocated: LiveData<Resource<List<SensorExtended>>>? = null
    internal var actuatorList: LiveData<Resource<List<ActuatorExtended>>>? = null
    internal var actuatorListMainDashboard: LiveData<Resource<List<ActuatorExtended>>>? = null
    internal var actuatorListLocated: LiveData<Resource<List<ActuatorExtended>>>? = null
    internal var allSensorsInMainDashboardLastValues: LiveData<Resource<List<DataValue>>>? = null

    /* Functions for managing the CRUD operations over networks */

    val listOfNetworksBlocking: List<Network>?
        get() {
            try {
                return this.dotDatabase.networksDao().allBlocking
            } catch (e: Exception) {
                return null
            }

        }

    val listOfNetworks: LiveData<Resource<List<NetworkExtended>>>?
        get() {
            try {
                if (this.networkList == null) {
                    val elementTypes = arrayOf<Enumerators.ElementType>(Enumerators.ElementType.NETWORK)
                    val logLevels = arrayOf<Enumerators.LogLevel>(Enumerators.LogLevel.WARN, Enumerators.LogLevel.ERROR)
                    this.networkList = LiveDataResource {
                        this.dotDatabase.networksDao().getAllExtended(elementTypes, logLevels)
                    }
                }
                return this.networkList
            } catch (e: Exception) {
                return null
            }

        }

    /* Functions for managing the CRUD operations over sensors */

    val listOfSensorsBlocking: List<Sensor>?
        get() {
            try {
                return this.dotDatabase.sensorsDao().allBlocking
            } catch (e: Exception) {
                return null
            }

        }

    val listOfSensors: LiveData<Resource<List<SensorExtended>>>?
        get() {
            try {
                if (this.sensorList == null) {
                    val elementTypes = arrayOf<Enumerators.ElementType>(Enumerators.ElementType.SENSOR)
                    val logLevels = arrayOf<Enumerators.LogLevel>(Enumerators.LogLevel.WARN, Enumerators.LogLevel.ERROR)
                    this.sensorList = LiveDataResource {
                        this.dotDatabase.sensorsDao().getAllExtended(elementTypes, logLevels)
                    }
                }
                return this.sensorList
            } catch (e: Exception) {
                return null
            }

        }

    val listOfSensorsMainDashboard: LiveData<Resource<List<SensorExtended>>>?
        get() {
            try {
                if (this.sensorListMainDashboard == null) {
                    val elementTypes = arrayOf<Enumerators.ElementType>(Enumerators.ElementType.SENSOR)
                    val logLevels = arrayOf<Enumerators.LogLevel>(Enumerators.LogLevel.WARN, Enumerators.LogLevel.ERROR)
                    this.sensorListMainDashboard = LiveDataResource {
                        this.dotDatabase.sensorsDao()
                            .getAllExtendedToBeShownInMainDashboard(elementTypes, logLevels)
                    }
                }
                return this.sensorListMainDashboard
            } catch (e: Exception) {
                return null
            }

        }

    val listOfSensorsLocated: LiveData<Resource<List<SensorExtended>>>?
        get() {
            try {
                if (this.sensorListLocated == null) {
                    val elementTypes = arrayOf<Enumerators.ElementType>(Enumerators.ElementType.SENSOR)
                    val logLevels = arrayOf<Enumerators.LogLevel>(Enumerators.LogLevel.WARN, Enumerators.LogLevel.ERROR)
                    this.sensorListLocated = LiveDataResource {
                        this.dotDatabase.sensorsDao().getAllExtendedLocated(elementTypes, logLevels)
                    }
                }
                return this.sensorListLocated
            } catch (e: Exception) {
                return null
            }

        }

    /* Functions for managing the CRUD operations over actuators */

    val listOfActuators: LiveData<Resource<List<ActuatorExtended>>>?
        get() {
            try {
                if (this.actuatorList == null) {
                    val elementTypes = arrayOf<Enumerators.ElementType>(Enumerators.ElementType.ACTUATOR)
                    val logLevels = arrayOf<Enumerators.LogLevel>(Enumerators.LogLevel.WARN, Enumerators.LogLevel.ERROR)
                    this.actuatorList = LiveDataResource {
                        this.dotDatabase.actuatorsDao().getAllExtended(elementTypes, logLevels)
                    }
                }
                return this.actuatorList
            } catch (e: Exception) {
                return null
            }

        }

    val listOfActuatorsMainDashboard: LiveData<Resource<List<ActuatorExtended>>>?
        get() {
            try {
                if (this.actuatorListMainDashboard == null) {
                    val elementTypes = arrayOf<Enumerators.ElementType>(Enumerators.ElementType.ACTUATOR)
                    val logLevels = arrayOf<Enumerators.LogLevel>(Enumerators.LogLevel.WARN, Enumerators.LogLevel.ERROR)
                    this.actuatorListMainDashboard = LiveDataResource {
                        this.dotDatabase.actuatorsDao()
                            .getAllExtendedToBeShownInMainDashboard(elementTypes, logLevels)
                    }
                }
                return this.actuatorListMainDashboard
            } catch (e: Exception) {
                return null
            }

        }

    val listOfActuatorsLocated: LiveData<Resource<List<ActuatorExtended>>>?
        get() {
            try {
                if (this.actuatorListLocated == null) {
                    val elementTypes = arrayOf<Enumerators.ElementType>(Enumerators.ElementType.ACTUATOR)
                    val logLevels = arrayOf<Enumerators.LogLevel>(Enumerators.LogLevel.WARN, Enumerators.LogLevel.ERROR)
                    this.actuatorListLocated = LiveDataResource {
                        this.dotDatabase.actuatorsDao()
                            .getAllExtendedLocated(elementTypes, logLevels)
                    }
                }
                return this.actuatorListLocated
            } catch (e: Exception) {
                return null
            }

        }

    /* Functions for managing request operations over data values stored */

    val lastValuesFromAllMainDashboard: LiveData<Resource<List<DataValue>>>?
        get() {
            try {
                if (this.allSensorsInMainDashboardLastValues == null) {
                    this.allSensorsInMainDashboardLastValues = LiveDataResource {
                        this.dotDatabase.dataValuesDao().lastValuesForAllInMainDashboard
                    }
                }
                return this.allSensorsInMainDashboardLastValues
            } catch (e: Exception) {
                return null
            }

        }

    /* Functions for managing request operations over logs stored */

    val lastConfigurationLogs: LiveData<Resource<List<Log>>>?
        get() {
            try {
                val logLevels = arrayOf<Enumerators.LogLevel>(Enumerators.LogLevel.INFO,
                    Enumerators.LogLevel.WARN, Enumerators.LogLevel.ERROR)
                return LiveDataResource {
                    this.dotDatabase.logsDao().getAllLastHundredLogs(logLevels)
                }
            } catch (e: Exception) {
                return null
            }

        }

    val lastNotificationLogsInMainDashboard: LiveData<Resource<List<Log>>>?
        get() {
            try {
                val logLevels = arrayOf<Enumerators.LogLevel>(Enumerators.LogLevel.NOTIF_NONE,
                    Enumerators.LogLevel.NOTIF_WARN, Enumerators.LogLevel.NOTIF_CRITICAL)
                return LiveDataResource {
                    this.dotDatabase.logsDao().getLastLogForSensorElementsInMainDashboard(logLevels)
                }
            } catch (e: Exception) {
                return null
            }

        }

    init {
        initializeSubscriptionsToDataValuesAndLogs()
    }

    /* Functions to be executed when started the repository */

    private fun initializeSubscriptionsToDataValuesAndLogs() {
        RxHelper.subscribeToAllSensorsData (Consumer {
                dataValue ->
            dbExecutorDataInsert.execute {
                try {
                    dotDatabase.dataValuesDao().insert(dataValue)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
        RxHelper.subscribeToAllLogs ( Consumer {
                log ->
            dbExecutorDataInsert.execute {
                try {
                    dotDatabase.logsDao().insert(log)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    fun getNetwork(networkId: Int): LiveData<Resource<NetworkExtended>> {
        val elementTypes = arrayOf<Enumerators.ElementType>(Enumerators.ElementType.NETWORK)
        val logLevels = arrayOf<Enumerators.LogLevel>(Enumerators.LogLevel.WARN, Enumerators.LogLevel.ERROR)
        return LiveDataResource {
            this.dotDatabase.networksDao().findExtendedById(networkId, elementTypes, logLevels)
        }
    }

    fun createNetwork(network: Network): LiveData<Resource<*>> {
        val result = MutableLiveData<Resource<*>>()
        result.postValue(Resource.loading<Any>(null))
        dbExecutorManagement.execute {
            try {
                val id = dotDatabase.networksDao().insert(network).toInt()
                network.id = id
                RxHelper.publishNetworkManagementChange(
                    Pair(
                        network,
                        Enumerators.ElementManagementFunction.CREATE
                    )
                )
                result.postValue(Resource.success<Any>(null))
            } catch (e: Exception) {
                result.postValue(Resource.error<Any>(e, null))
            }
        }
        return result
    }

    fun updateNetwork(network: Network): LiveData<Resource<*>> {
        val result = MutableLiveData<Resource<*>>()
        result.postValue(Resource.loading<Any>(null))
        dbExecutorManagement.execute {
            try {
                dotDatabase.networksDao().update(network)
                RxHelper.publishNetworkManagementChange(
                    Pair(
                        network,
                        Enumerators.ElementManagementFunction.UPDATE
                    )
                )
                result.postValue(Resource.success<Any>(null))
            } catch (e: Exception) {
                result.postValue(Resource.error<Any>(e, null))
            }
        }
        return result
    }

    fun deleteNetwork(network: Network): LiveData<Resource<*>> {
        val result = MutableLiveData<Resource<*>>()
        result.postValue(Resource.loading<Any>(null))
        dbExecutorManagement.execute {
            try {
                dotDatabase.networksDao().deleteExtended(network)
                RxHelper.publishNetworkManagementChange(
                    Pair(
                        network,
                        Enumerators.ElementManagementFunction.DELETE
                    )
                )
                result.postValue(Resource.success<Any>(null))
            } catch (e: Exception) {
                result.postValue(Resource.error<Any>(e, null))
            }
        }
        return result
    }

    fun getListOfSensorsFromSameNetwork(networkId: Int): LiveData<Resource<List<Sensor>>>? {
        try {
            return LiveDataResource {
                this.dotDatabase.sensorsDao().getAllFromSameNetwork(networkId)
            }
        } catch (e: Exception) {
            return null
        }

    }

    fun getSensor(sensorId: Int): LiveData<Resource<SensorExtended>>? {
        try {
            val elementTypes = arrayOf<Enumerators.ElementType>(Enumerators.ElementType.SENSOR)
            val logLevels = arrayOf<Enumerators.LogLevel>(Enumerators.LogLevel.WARN, Enumerators.LogLevel.ERROR)
            return LiveDataResource {
                this.dotDatabase.sensorsDao().findByIdExtended(sensorId, elementTypes, logLevels)
            }
        } catch (e: Exception) {
            return null
        }

    }

    fun createSensor(sensor: Sensor): LiveData<Resource<*>> {
        val result = MutableLiveData<Resource<*>>()
        result.postValue(Resource.loading<Any>(null))
        dbExecutorManagement.execute {
            try {
                val id = dotDatabase.sensorsDao().insert(sensor).toInt()
                sensor.id = id
                RxHelper.publishSensorManagementChange(
                    Pair(
                        sensor,
                        Enumerators.ElementManagementFunction.CREATE
                    )
                )
                result.postValue(Resource.success<Any>(null))
            } catch (e: Exception) {
                result.postValue(Resource.error<Any>(e, null))
            }
        }
        return result
    }

    fun updateSensor(sensor: Sensor): LiveData<Resource<*>> {
        val result = MutableLiveData<Resource<*>>()
        result.postValue(Resource.loading<Any>(null))
        dbExecutorManagement.execute {
            try {
                dotDatabase.sensorsDao().update(sensor)
                RxHelper.publishSensorManagementChange(
                    Pair(
                        sensor,
                        Enumerators.ElementManagementFunction.UPDATE
                    )
                )
                result.postValue(Resource.success<Any>(null))
            } catch (e: Exception) {
                result.postValue(Resource.error<Any>(e, null))
            }
        }
        return result
    }

    fun deleteSensor(sensor: Sensor): LiveData<Resource<*>> {
        val result = MutableLiveData<Resource<*>>()
        result.postValue(Resource.loading<Any>(null))
        dbExecutorManagement.execute {
            try {
                dotDatabase.sensorsDao().deleteExtended(sensor)
                RxHelper.publishSensorManagementChange(
                    Pair(
                        sensor,
                        Enumerators.ElementManagementFunction.DELETE
                    )
                )
                result.postValue(Resource.success<Any>(null))
            } catch (e: Exception) {
                result.postValue(Resource.error<Any>(e, null))
            }
        }
        return result
    }

    fun getListOfActuatorsFromSameNetwork(NetworkId: Int): LiveData<Resource<List<Actuator>>>? {
        try {
            return LiveDataResource {
                this.dotDatabase.actuatorsDao().getAllFromSameNetwork(NetworkId)
            }
        } catch (e: Exception) {
            return null
        }

    }

    fun getActuator(actuatorId: Int): LiveData<Resource<ActuatorExtended>>? {
        try {
            val elementTypes = arrayOf<Enumerators.ElementType>(Enumerators.ElementType.ACTUATOR)
            val logLevels = arrayOf<Enumerators.LogLevel>(Enumerators.LogLevel.WARN, Enumerators.LogLevel.ERROR)
            return LiveDataResource {
                this.dotDatabase.actuatorsDao()
                    .findByIdExtended(actuatorId, elementTypes, logLevels)
            }
        } catch (e: Exception) {
            return null
        }

    }

    fun createActuator(actuator: Actuator): LiveData<Resource<*>> {
        val result = MutableLiveData<Resource<*>>()
        result.postValue(Resource.loading<Any>(null))
        dbExecutorManagement.execute {
            try {
                val id = dotDatabase.actuatorsDao().insert(actuator).toInt()
                actuator.id = id
                RxHelper.publishActuatorManagementChange(
                    Pair(
                        actuator,
                        Enumerators.ElementManagementFunction.CREATE
                    )
                )
                result.postValue(Resource.success<Any>(null))
            } catch (e: Exception) {
                result.postValue(Resource.error<Any>(e, null))
            }
        }
        return result
    }

    fun updateActuator(actuator: Actuator): LiveData<Resource<*>> {
        val result = MutableLiveData<Resource<*>>()
        result.postValue(Resource.loading<Any>(null))
        dbExecutorManagement.execute {
            try {
                dotDatabase.actuatorsDao().update(actuator)
                RxHelper.publishActuatorManagementChange(
                    Pair(
                        actuator,
                        Enumerators.ElementManagementFunction.UPDATE
                    )
                )
                result.postValue(Resource.success<Any>(null))
            } catch (e: Exception) {
                result.postValue(Resource.error<Any>(e, null))
            }
        }
        return result
    }

    fun deleteActuator(actuator: Actuator): LiveData<Resource<*>> {
        val result = MutableLiveData<Resource<*>>()
        result.postValue(Resource.loading<Any>(null))
        dbExecutorManagement.execute {
            try {
                dotDatabase.actuatorsDao().deleteExtended(actuator)
                RxHelper.publishActuatorManagementChange(
                    Pair(
                        actuator,
                        Enumerators.ElementManagementFunction.DELETE
                    )
                )
                result.postValue(Resource.success<Any>(null))
            } catch (e: Exception) {
                result.postValue(Resource.error<Any>(e, null))
            }
        }
        return result
    }

    /* Functions for managing users request to reload or update info */

    fun requestSensorReload(sensor: Sensor): LiveData<Resource<*>> {
        val result = MutableLiveData<Resource<*>>()
        result.postValue(Resource.loading<Any>(null))
        try {
            RxHelper.publishSensorReloadRequest(sensor)
            result.postValue(Resource.success<Any>(null))
        } catch (e: Exception) {
            result.postValue(Resource.error<Any>(e, null))
        }

        return result
    }

    fun requestSensorReloadInstant(id: Int) {
        try {
            dbExecutorManagement.execute {
                try {
                    val sensor = dotDatabase.sensorsDao().findByIdInstant(id)
                    RxHelper.publishSensorReloadRequest(sensor)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun updateActuatorData(actuator: Actuator, data: String): LiveData<Resource<*>> {
        val result = MutableLiveData<Resource<*>>()
        result.postValue(Resource.loading<Any>(null))
        try {
            RxHelper.publishActuatorUpdate(actuator, data)
            result.postValue(Resource.success<Any>(null))
        } catch (e: Exception) {
            result.postValue(Resource.error<Any>(e, null))
        }

        return result
    }

    fun findLastValuesForSensorId(id: Int): LiveData<Resource<DataValue>>? {
        try {
            return LiveDataResource { this.dotDatabase.dataValuesDao().findLastForSensorId(id) }
        } catch (e: Exception) {
            return null
        }

    }

    fun findLastValuesForSensorIds(ids: IntArray): LiveData<Resource<List<DataValue>>>? {
        try {
            return LiveDataResource { this.dotDatabase.dataValuesDao().findLastForSensorIds(ids) }
        } catch (e: Exception) {
            return null
        }

    }

    fun getLastValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        try {
            return LiveDataResource {
                this.dotDatabase.dataValuesDao().getLastValuesForSensorId(id)
            }
        } catch (e: Exception) {
            return null
        }

    }

    fun getAvgLastOneDayValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        try {
            val cal = Calendar.getInstance()
            val today = cal.time
            cal.add(Calendar.HOUR, -24)
            val prevDay = cal.time
            return LiveDataResource {
                this.dotDatabase.dataValuesDao().getAvgHourValuesForSensorId(id, prevDay)
            }
        } catch (e: Exception) {
            return null
        }

    }

    fun getAvgLastOneWeekValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        try {
            val cal = Calendar.getInstance()
            val today = cal.time
            cal.add(Calendar.DAY_OF_MONTH, -7)
            val prevWeek = cal.time
            return LiveDataResource {
                this.dotDatabase.dataValuesDao().getAvgWeekdayValuesForSensorId(id, prevWeek)
            }
        } catch (e: Exception) {
            return null
        }

    }

    fun getAvgLastOneMonthValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        try {
            val cal = Calendar.getInstance()
            val today = cal.time
            cal.add(Calendar.MONTH, -1)
            val prevMonth = cal.time
            return LiveDataResource {
                this.dotDatabase.dataValuesDao().getAvgPerMonthDayValuesForSensorId(id, prevMonth)
            }
        } catch (e: Exception) {
            return null
        }

    }

    fun getAvgLastOneYearValuesForSensorId(id: Int): LiveData<Resource<List<DataValue>>>? {
        try {
            val cal = Calendar.getInstance()
            val today = cal.time
            cal.add(Calendar.YEAR, -1)
            val prevYear = cal.time
            return LiveDataResource {
                this.dotDatabase.dataValuesDao().getAvgPerMonthValuesForSensorId(id, prevYear)
            }
        } catch (e: Exception) {
            return null
        }

    }

    fun getLastNotificationLogForElement(
        elementId: Int,
        elementType: Enumerators.ElementType
    ): LiveData<Log>? {
        try {
            val logLevels = arrayOf<Enumerators.LogLevel>(Enumerators.LogLevel.NOTIF_NONE,
                Enumerators.LogLevel.NOTIF_WARN, Enumerators.LogLevel.NOTIF_CRITICAL)
            return this.dotDatabase.logsDao().findLastForElementId(
                elementId,
                elementType, logLevels
            )
        } catch (e: Exception) {
            return null
        }

    }

    fun getLastLogsForElement(
        id: Int,
        elementType: Enumerators.ElementType
    ): LiveData<Resource<List<Log>>>? {
        try {
            return LiveDataResource {
                this.dotDatabase.logsDao().getLastHundredLogsForElementId(id, elementType)
            }
        } catch (e: Exception) {
            return null
        }

    }

    fun getSensorInfoForWidget(id: Int, consumer: androidx.core.util.Consumer<SensorWidgetItem>) {
        try {
            dbExecutorManagement.execute {
                try {
                    val sensorInfo = dotDatabase.sensorsDao().findByIdForWidgetInstant(id)
                    consumer.accept(sensorInfo)
                } catch (e: Exception) {
                    consumer.accept(null)
                }
            }
        } catch (e: Exception) {
            consumer.accept(null)
        }

    }

    fun getSensorInfoForWidgets(ids: IntArray, consumer: androidx.core.util.Consumer<List<SensorWidgetItem>>) {
        try {
            dbExecutorManagement.execute {
                try {
                    val sensorInfoList = dotDatabase.sensorsDao().getAllForWidgetsInstant(ids)
                    consumer.accept(sensorInfoList)
                } catch (e: Exception) {
                    consumer.accept(null)
                }
            }
        } catch (e: Exception) {
            consumer.accept(null)
        }

    }

    companion object {

        /* Static functions to be used outside */

        fun checkThresholdsForDataReceived(context: Context, sensor: Sensor, dataReceived: String) {
            try {
                val state = DataHelper.getNotificationStatus(
                    dataReceived,
                    sensor.dataType, sensor.thresholdAboveWarning, sensor.thresholdAboveCritical,
                    sensor.thresholdBelowWarning, sensor.thresholdBelowCritical,
                    sensor.thresholdEqualsWarning, sensor.thresholdEqualsCritical
                )
                if (state != null) {
                    when (state) {
                        Enumerators.NotificationState.NONE -> NotificationsHelper.processStateNotificationForSensor(
                            context,
                            sensor,
                            Enumerators.NotificationType.NONE
                        )
                        Enumerators.NotificationState.WARNING -> NotificationsHelper.processStateNotificationForSensor(
                            context,
                            sensor,
                            Enumerators.NotificationType.WARNING
                        )
                        Enumerators.NotificationState.CRITICAL -> NotificationsHelper.processStateNotificationForSensor(
                            context,
                            sensor,
                            Enumerators.NotificationType.CRITICAL
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

}
