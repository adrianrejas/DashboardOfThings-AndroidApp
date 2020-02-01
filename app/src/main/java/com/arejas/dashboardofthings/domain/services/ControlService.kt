package com.arejas.dashboardofthings.domain.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.data.interfaces.DotRepository
import com.arejas.dashboardofthings.data.sources.network.HttpNetworkInterfaceHelper
import com.arejas.dashboardofthings.data.sources.network.MqttNetworkInterfaceHelper
import com.arejas.dashboardofthings.data.sources.network.NetworkInterfaceHelper
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.ui.activities.MainDashboardActivity
import com.arejas.dashboardofthings.presentation.ui.notifications.NotificationsHelper
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper
import com.arejas.dashboardofthings.presentation.ui.widget.SensorWidgetService
import com.arejas.dashboardofthings.utils.Enumerators
import com.arejas.dashboardofthings.utils.rx.RxHelper

import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.Executor

import javax.inject.Inject
import javax.inject.Named

import dagger.android.AndroidInjection
import io.reactivex.functions.Consumer

class ControlService : Service() {

    internal var networkHelpers: MutableMap<Int, NetworkInterfaceHelper> = HashMap()

    private val networkHelpersLock = Any()

    @Inject
    internal var dotRepository: DotRepository? = null
    @Inject
    @Named("dbExecutorManagement")
    internal var dbExecutorManagement: Executor? = null

    private var networks: List<Network>? = null
    private var sensors: List<Sensor>? = null

    private var initiated: Boolean = false

    override fun onCreate() {

        startForeground(
            NotificationsHelper.FOREGROUND_SERVICE_NOTIFICATION_ID,
            NotificationsHelper.showNotificationForegroundService(applicationContext)
        )

        AndroidInjection.inject(this)

        if (!initiated) {
            dbExecutorManagement!!.execute {
                initializeNetworkHelpers()
                initializeSubscriptionsToNetworksAndSensorsManagementChanges()
                initializeSubscriptionsToActuatorDataUpdates()
                initializeSubscriptionsToSensorReloadRequests()
                initializeSubscriptionsToDataReceived()
            }
        }
        initiated = true
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    fun initializeNetworkHelpers() {
        synchronized(networkHelpersLock) {
            sensors = dotRepository!!.listOfSensorsBlocking
            networks = dotRepository!!.listOfNetworksBlocking
            if (networks != null) {
                for (network in networks!!) {
                    initNetworkHelper(network)
                }
            }
        }
    }

    override fun onDestroy() {
        closeNetworkHelpers()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun initializeSubscriptionsToNetworksAndSensorsManagementChanges() {
        RxHelper.subscribeToAllNetworskManagementChanges(Consumer { networkManagementPair ->
            if (networkManagementPair != null)
                when (networkManagementPair.second) {
                    Enumerators.ElementManagementFunction.CREATE -> initNetworkHelper(
                        networkManagementPair.first
                    )
                    Enumerators.ElementManagementFunction.UPDATE -> restartNetworkHelper(
                        networkManagementPair.first
                    )
                    Enumerators.ElementManagementFunction.DELETE -> closeNetworkHelper(
                        networkManagementPair.first
                    )
                } })
    }

    private fun initializeSubscriptionsToActuatorDataUpdates() {
        RxHelper.subscribeToAllActuatorUpdates(Consumer { message ->
            if (message != null)
                sendActuatorUpdateToNetworkHelper(message.actuator!!, message.data!!)
        } )
    }

    private fun initializeSubscriptionsToSensorReloadRequests() {
        RxHelper.subscribeToAllSensorReloadRequests(Consumer { sensor ->
            if (sensor != null)
                sendSensorReloadRequestToNetworkHelper(sensor)
        } )
    }

    private fun initializeSubscriptionsToDataReceived() {
        RxHelper.subscribeToAllSensorsData(Consumer { dataValue ->
            if (dataValue != null) {
                val widgetId = SensorWidgetService.getWidgetIdForSensorId(
                    applicationContext,
                    dataValue.sensorId!!
                )
                if (widgetId != SensorWidgetService.UNKNOWN_ELEMENT_ID) {
                    SensorWidgetService.startActionUpdateWidgetSetData(
                        applicationContext,
                        widgetId,
                        dataValue.value
                    )
                }
            }
        } )
    }

    fun closeNetworkHelpers() {
        synchronized(networkHelpersLock) {
            if (sensors != null) {
                for (sensor in sensors!!) {
                    unregisterSensorInNetwork(sensor)
                }
            }
            if (networks != null) {
                for (network in networks!!) {
                    closeNetworkHelper(network)
                }
            }
        }
    }

    private fun initNetworkHelper(network: Network): Boolean {
        synchronized(networkHelpersLock) {
            try {
                closeNetworkHelper(network)
                var helper: NetworkInterfaceHelper? = null
                when (network.networkType) {
                    Enumerators.NetworkType.HTTP -> helper = HttpNetworkInterfaceHelper(network)
                    Enumerators.NetworkType.MQTT -> helper = MqttNetworkInterfaceHelper(network)
                }
                if (helper != null) {
                    val initialSensors = ArrayList<Sensor>()
                    if (sensors != null) {
                        for (sensor in sensors!!) {
                            if (sensor.networkId == network.id) {
                                initialSensors.add(sensor)
                            }
                        }
                    }
                    helper.initNetworkInterface(
                        applicationContext,
                        initialSensors.toTypedArray()
                    )
                    this.networkHelpers[network.id!!] = helper
                    return true
                }
                return false
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }

        }
    }

    private fun closeNetworkHelper(network: Network): Boolean {
        synchronized(networkHelpersLock) {
            try {
                if (this.networkHelpers.containsKey(network.id)) {
                    this.networkHelpers[network.id]!!.closeNetworkInterface(applicationContext)
                    this.networkHelpers.remove(network.id)
                    return true
                } else {
                    return false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }

        }
    }

    private fun restartNetworkHelper(network: Network): Boolean {
        synchronized(networkHelpersLock) {
            closeNetworkHelper(network)
            return initNetworkHelper(network)
        }
    }

    private fun registerSensorInNetwork(sensor: Sensor): Boolean {
        synchronized(networkHelpersLock) {
            try {
                return if (networkHelpers.containsKey(sensor.networkId)) {
                    networkHelpers[sensor.networkId]!!.configureSensorReceiving(
                        applicationContext,
                        sensor
                    )
                } else false
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }

        }
    }

    private fun unregisterSensorInNetwork(sensor: Sensor): Boolean {
        synchronized(networkHelpersLock) {
            try {
                return if (networkHelpers.containsKey(sensor.networkId)) {
                    networkHelpers[sensor.networkId]!!.unconfigureSensorReceiving(
                        applicationContext,
                        sensor
                    )
                } else false
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }

        }
    }

    private fun restartRegisterSensorInNetwork(sensor: Sensor): Boolean {
        synchronized(networkHelpersLock) {
            return if (unregisterSensorInNetwork(sensor)) {
                registerSensorInNetwork(sensor)
            } else false
        }
    }

    private fun sendActuatorUpdateToNetworkHelper(actuator: Actuator, data: String): Boolean {
        synchronized(networkHelpersLock) {
            try {
                return if (networkHelpers.containsKey(actuator.networkId)) {
                    networkHelpers[actuator.networkId]!!.sendActuatorData(
                        applicationContext,
                        actuator,
                        data
                    )
                } else false
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }

        }
    }

    private fun sendSensorReloadRequestToNetworkHelper(sensor: Sensor): Boolean {
        synchronized(networkHelpersLock) {
            try {
                return if (networkHelpers.containsKey(sensor.networkId)) {
                    networkHelpers[sensor.networkId]!!.requestSensorReload(
                        applicationContext,
                        sensor
                    )
                } else false
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }

        }
    }

}
