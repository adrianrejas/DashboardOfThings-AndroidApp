package com.arejas.dashboardofthings.data.sources.network

import android.content.Context
import android.os.Bundle

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.data.sources.network.http.HttpRequestIntentService
import com.arejas.dashboardofthings.data.sources.network.http.HttpSensorRequestJobService
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.utils.Enumerators
import com.arejas.dashboardofthings.utils.rx.RxHelper
import com.firebase.jobdispatcher.FirebaseJobDispatcher
import com.firebase.jobdispatcher.GooglePlayDriver
import com.firebase.jobdispatcher.Job
import com.firebase.jobdispatcher.Lifetime
import com.firebase.jobdispatcher.RetryStrategy
import com.firebase.jobdispatcher.Trigger
import com.google.gson.Gson

class HttpNetworkInterfaceHelper(network: Network) : NetworkInterfaceHelper(network) {

    private var dispatcher: FirebaseJobDispatcher? = null

    override fun initNetworkInterface(context: Context, sensors: Array<Sensor>): Boolean {
        // Create a new dispatcher using the Google Play driver.
        dispatcher = FirebaseJobDispatcher(GooglePlayDriver(context))
        for (sensor in sensors) {
            configureSensorReceiving(context, sensor)
        }
        return true
    }

    override fun closeNetworkInterface(context: Context): Boolean {
        // cancell all jobs and set the job dispatcher to null
        dispatcher!!.cancelAll()
        dispatcher = null
        return true
    }

    //TODO
    /* For passing the database parameters to the job service, right now they are converted in
     * JSON strings and decoded again when received. This is because using the database entities
     * as parcelable objects caused problems in some of the devices tested. For now, we're using GSON
     * for parsing because, although less optimized, is working on every situation. But I'll keep up
     * researching on this.*/
    /*
    In HTTP cases, when configured a sensor, we take the interval defined for it and configure a
    Firebase JobDispatcher Job for launching it on a periodical basis. The job dispatcher will send
    an Intent to the intent service in charge of sensing HTTP requests on a worker thread.
     */
    override fun configureSensorReceiving(context: Context, sensor: Sensor): Boolean {
        try {
            val extras = Bundle()
            val gson = Gson()
            val networkStr = gson.toJson(network)
            val sensorStr = gson.toJson(sensor)
            extras.putString(HttpSensorRequestJobService.NETWORK_OBJECT, networkStr)
            extras.putString(HttpSensorRequestJobService.SENSOR_OBJECT, sensorStr)
            val sensorJob = dispatcher!!.newJobBuilder()
                .setService(HttpSensorRequestJobService::class.java)
                .setTag(Integer.toString(sensor.id!!))
                .setRecurring(true)
                .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                .setTrigger(
                    Trigger.executionWindow(
                        sensor.httpSecondsBetweenRequests!!,
                        sensor.httpSecondsBetweenRequests!! + MARGIN_WINDOW_PERIODIC_SECONDS
                    )
                )
                .setReplaceCurrent(true)
                .setExtras(extras)
                .build()
            dispatcher!!.mustSchedule(sensorJob)
            registerSensor(sensor)
            return true
        } catch (e: Exception) {
            RxHelper.publishLog(
                sensor.id, Enumerators.ElementType.SENSOR,
                sensor.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_sensor_scheduling)
            )
            return false
        }

    }

    override fun unconfigureSensorReceiving(context: Context, sensor: Sensor): Boolean {
        unregisterSensor(sensor)
        dispatcher!!.cancel(Integer.toString(sensor.id!!))
        return true
    }

    override fun sendActuatorData(
        context: Context,
        actuator: Actuator,
        dataToSend: String
    ): Boolean {
        try {
            HttpRequestIntentService.startActionActuatorCommand(
                context,
                network,
                actuator,
                dataToSend
            )
            return true
        } catch (e: Exception) {
            RxHelper.publishLog(
                actuator.id, Enumerators.ElementType.ACTUATOR,
                actuator.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_actautor_send)
            )
            return false
        }

    }

    override fun requestSensorReload(context: Context, sensor: Sensor): Boolean {
        try {
            HttpRequestIntentService.startActionSensorRequest(context, network, sensor)
            return true
        } catch (e: Exception) {
            RxHelper.publishLog(
                sensor.id, Enumerators.ElementType.SENSOR,
                sensor.name, Enumerators.LogLevel.ERROR,
                context.getString(R.string.log_critical_sensor_scheduling)
            )
            return false
        }

    }

    companion object {

        private val MARGIN_WINDOW_PERIODIC_SECONDS = 30
    }

}
