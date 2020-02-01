package com.arejas.dashboardofthings.data.sources.network

import android.content.Context

import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor

import java.util.HashMap

abstract class NetworkInterfaceHelper(var network: Network) {

    internal var sensorsRegistered: MutableMap<Int, Sensor>

    private val sensorsLock = Any()

    init {
        sensorsRegistered = HashMap()
    }

    abstract fun initNetworkInterface(context: Context, sensors: Array<Sensor>): Boolean

    abstract fun closeNetworkInterface(context: Context): Boolean

    abstract fun configureSensorReceiving(context: Context, sensor: Sensor): Boolean

    abstract fun unconfigureSensorReceiving(context: Context, sensor: Sensor): Boolean

    abstract fun sendActuatorData(context: Context, actuator: Actuator, dataToSend: String): Boolean

    abstract fun requestSensorReload(context: Context, sensor: Sensor): Boolean

    fun getSensorsRegistered(): Map<Int, Sensor> {
        synchronized(sensorsLock) {
            val returnedMap = HashMap<Int, Sensor>()
            returnedMap.putAll(sensorsRegistered!!)
            return returnedMap
        }
    }

    fun setSensorsRegistered(sensorsRegistered: MutableMap<Int, Sensor>) {
        synchronized(sensorsLock) {
            this.sensorsRegistered = sensorsRegistered
        }
    }

    fun registerSensor(sensor: Sensor) {
        synchronized(sensorsLock) {
            this.sensorsRegistered!!.put(sensor.id!!, sensor)
        }
    }

    fun unregisterSensor(sensor: Sensor) {
        synchronized(sensorsLock) {
            this.sensorsRegistered!!.remove(sensor.id)
        }
    }
}
