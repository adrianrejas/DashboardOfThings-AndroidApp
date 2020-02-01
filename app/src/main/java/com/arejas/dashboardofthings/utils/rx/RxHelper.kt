package com.arejas.dashboardofthings.utils.rx

import android.util.Pair

import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.database.Log
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorSendDataUnit
import com.arejas.dashboardofthings.utils.Enumerators

import java.util.Date

import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject

object RxHelper {

    // Subject to communicate element management changes to Control Service
    private val networkManagementSubject =
        BehaviorSubject.create<Pair<Network, Enumerators.ElementManagementFunction>>()
    private val sensorManagementSubject =
        BehaviorSubject.create<Pair<Sensor, Enumerators.ElementManagementFunction>>()
    private val actuatorManagementSubject =
        BehaviorSubject.create<Pair<Actuator, Enumerators.ElementManagementFunction>>()

    // Subject to communicate actuator data updates to Control Service
    private val actuatorDataUpdateSubject = BehaviorSubject.create<ActuatorSendDataUnit>()

    // Subject to communicate sensor reload requests to Control Service
    private val sensorReloadRequestSubject = BehaviorSubject.create<Sensor>()

    // Subject to communicate data values received from sensors
    private val sensorDataSubject = BehaviorSubject.create<DataValue>()

    // Subject to communicate logs generated
    private val logSubject = BehaviorSubject.create<Log>()

    // Receive data values from all sensors
    fun subscribeToAllSensorsData(action: Consumer<DataValue>): Disposable {
        return sensorDataSubject.subscribe(action)
    }

    // Receive data values from one sensor
    fun subscribeToOneSensorData(
        sensorId: Int, action: Consumer<DataValue>
    ): Disposable {
        return sensorDataSubject
            .filter { dataValue -> sensorId == dataValue.sensorId }
            .subscribe(action)
    }

    // Send data values to those interested
    private fun publishSensorData(message: DataValue) {
        sensorDataSubject.onNext(message)
    }

    fun publishSensorData(sensorId: Int, sensorData: String) {
        try {
            val value = DataValue()
            value.sensorId = sensorId
            value.value = sensorData
            value.dateReceived = Date()
            publishSensorData(value)
        } catch (e: Exception) {
            e.printStackTrace()    // We don't report through RX this exception for avoiding loops
        }

    }

    // Receive logs from all elements
    fun subscribeToAllLogs(action: Consumer<Log>): Disposable {
        return logSubject.subscribe(action)
    }

    // Receive logs from all elements with a minimum log level
    fun subscribeToAllLogsWithMinLogLevel(
        logLevel: Enumerators.LogLevel, action: Consumer<Log>
    ): Disposable {
        return logSubject
            .filter { log -> logLevel.ordinal <= log.logLevel!!.ordinal }
            .subscribe(action)
    }

    // Receive logs from one element with a minimum log level if wanted
    fun subscribeToOneElementLogs(
        elementId: Int, elementType: Enumerators.ElementType,
        logLevel: Enumerators.LogLevel?,
        action: Consumer<Log>
    ): Disposable {
        return logSubject
            .filter { log ->
                elementId == log.elementId &&
                        elementType == log.elementType &&
                        (logLevel == null || logLevel.ordinal <= log.logLevel!!.ordinal)
            }
            .subscribe(action)
    }

    // Send logs to those interested
    private fun publishLog(message: Log) {
        logSubject.onNext(message)
    }

    fun publishLog(
        elementId: Int, elementType: Enumerators.ElementType,
        elementName: String, logLevel: Enumerators.LogLevel, message: String
    ) {
        try {
            val log = Log()
            log.elementId = elementId
            log.elementName = elementName
            log.elementType = elementType
            log.logLevel = logLevel
            log.logMessage = message
            log.dateRegistered = Date()
            publishLog(log)
        } catch (e: Exception) {
            e.printStackTrace()    // We don't report through RX this exception for avoiding loops
        }

    }

    // Receive data values from all sensors
    fun subscribeToAllActuatorUpdates(action: Consumer<ActuatorSendDataUnit>): Disposable {
        return actuatorDataUpdateSubject.subscribe(action)
    }

    // Receive data values from one sensor
    fun subscribeToOneActuatorUpdates(
        actuatorId: Int, action: Consumer<ActuatorSendDataUnit>
    ): Disposable {
        return actuatorDataUpdateSubject
            .filter { data -> actuatorId == data.actuator!!.id }
            .subscribe(action)
    }

    // Send actuator updates to those interested
    fun publishActuatorUpdate(actuator: Actuator, data: String) {
        val message = ActuatorSendDataUnit(actuator, data)
        actuatorDataUpdateSubject.onNext(message)
    }

    // Receive reload requests from all sensors
    fun subscribeToAllSensorReloadRequests(action: Consumer<Sensor>): Disposable {
        return sensorReloadRequestSubject.subscribe(action)
    }

    // Receive reload requests from one sensor
    fun subscribeToOneSensorReloadRequests(
        sensorId: Int, action: Consumer<Sensor>
    ): Disposable {
        return sensorReloadRequestSubject
            .filter { data -> sensorId == data.id }
            .subscribe(action)
    }

    // Send sensor reload reqeust to those interested
    fun publishSensorReloadRequest(message: Sensor) {
        sensorReloadRequestSubject.onNext(message)
    }

    // Receive management changes from all networks
    fun subscribeToAllNetworskManagementChanges(action: Consumer<Pair<Network, Enumerators.ElementManagementFunction>>): Disposable {
        return networkManagementSubject.subscribe(action)
    }

    // Send network management changes to those interested
    fun publishNetworkManagementChange(message: Pair<Network, Enumerators.ElementManagementFunction>) {
        networkManagementSubject.onNext(message)
    }

    // Receive management changes from all sensors
    fun subscribeToAllSensorsManagementChanges(action: Consumer<Pair<Sensor, Enumerators.ElementManagementFunction>>): Disposable {
        return sensorManagementSubject.subscribe(action)
    }

    // Send sensor management changes to those interested
    fun publishSensorManagementChange(message: Pair<Sensor, Enumerators.ElementManagementFunction>) {
        sensorManagementSubject.onNext(message)
    }

    // Receive management changes from all actuators
    fun subscribeToAllActuatorsManagementChanges(action: Consumer<Pair<Actuator, Enumerators.ElementManagementFunction>>): Disposable {
        return actuatorManagementSubject.subscribe(action)
    }

    // Send actuator management changes to those interested
    fun publishActuatorManagementChange(message: Pair<Actuator, Enumerators.ElementManagementFunction>) {
        actuatorManagementSubject.onNext(message)
    }
}
