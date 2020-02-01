package com.arejas.dashboardofthings.data.sources.network.http

import android.app.IntentService
import android.content.Intent
import android.content.Context

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.utils.Enumerators
import com.arejas.dashboardofthings.utils.rx.RxHelper
import com.google.gson.Gson

import java.util.concurrent.Executor

import javax.inject.Inject
import javax.inject.Named

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class HttpRequestIntentService : IntentService("HttpRequestIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        try {
            if (intent != null) {
                val action = intent.action
                if (ACTION_SENSOR_REQUEST == action) {
                    val gson = Gson()
                    val networkStr = intent.extras!!.getString(EXTRA_NETWORK)
                    val sensorStr = intent.extras!!.getString(EXTRA_SENSOR)
                    val network = gson.fromJson(networkStr, Network::class.java)
                    val sensor = gson.fromJson(sensorStr, Sensor::class.java)
                    handleActionSensorRequest(network, sensor)
                } else if (ACTION_ACTUATOR_COMMNAND == action) {
                    val gson = Gson()
                    val networkStr = intent.extras!!.getString(EXTRA_NETWORK)
                    val actuatorStr = intent.extras!!.getString(EXTRA_ACTUATOR)
                    val network = gson.fromJson(networkStr, Network::class.java)
                    val actuator = gson.fromJson(actuatorStr, Actuator::class.java)
                    val dataToSend = intent.getStringExtra(EXTRA_DATATOSEND)
                    handleActionActuatorCommand(network, actuator, dataToSend)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Handle action Sensor request in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionSensorRequest(network: Network, sensor: Sensor) {
        try {
            HttpRequestHelper.sendSensorDataHttpRequest(applicationContext, network, sensor)
        } catch (e: Exception) {
            RxHelper.publishLog(
                network.id, Enumerators.ElementType.NETWORK,
                network.name, Enumerators.LogLevel.ERROR,
                getString(R.string.log_critical_unexpected_http_network)
            )
        }

    }

    /**
     * Handle action Actuator command in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionActuatorCommand(
        network: Network,
        actuator: Actuator,
        dataToSend: String
    ) {
        try {
            HttpRequestHelper.sendActuatorCommand(applicationContext, network, actuator, dataToSend)
        } catch (e: Exception) {
            RxHelper.publishLog(
                network.id, Enumerators.ElementType.NETWORK,
                network.name, Enumerators.LogLevel.ERROR,
                getString(R.string.log_critical_unexpected_http_network)
            )
        }

    }

    companion object {

        //TODO
        /* For passing the database parameters to the intent service, right now they are converted in
    * JSON strings and decoded again when received. This is because using the database entities
    * as parcelable objects caused problems in some of the devices tested. For now, we're using GSON
    * for parsing because, although less optimized, is working on every situation. But I'll keep up
    * researching on this.*/

        // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
        private val ACTION_SENSOR_REQUEST =
            "com.arejas.dashboardofthings.data.sources.network.http.action.SENSOR_REQUEST"
        private val ACTION_ACTUATOR_COMMNAND =
            "com.arejas.dashboardofthings.data.sources.network.http.action.ACTUATOR_COMMNAND"

        private val EXTRA_NETWORK =
            "com.arejas.dashboardofthings.data.sources.network.http.extra.NETWORK"
        private val EXTRA_SENSOR =
            "com.arejas.dashboardofthings.data.sources.network.http.extra.SENSOR"
        private val EXTRA_ACTUATOR =
            "com.arejas.dashboardofthings.data.sources.network.http.extra.ACTUATOR"
        private val EXTRA_DATATOSEND =
            "com.arejas.dashboardofthings.data.sources.network.http.extra.DATATOSEND"

        /**
         * Starts this service to perform action SENSOR REQUEST with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionSensorRequest(context: Context, network: Network, sensor: Sensor) {
            val intent = Intent(context, HttpRequestIntentService::class.java)
            intent.action = ACTION_SENSOR_REQUEST
            val gson = Gson()
            val networkStr = gson.toJson(network)
            val sensorStr = gson.toJson(sensor)
            intent.putExtra(EXTRA_NETWORK, networkStr)
            intent.putExtra(EXTRA_SENSOR, sensorStr)
            context.startService(intent)
        }

        /**
         * Starts this service to perform action ACTUATOR COMMAND with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionActuatorCommand(
            context: Context,
            network: Network,
            actuator: Actuator,
            dataToSend: String
        ) {
            val intent = Intent(context, HttpRequestIntentService::class.java)
            intent.action = ACTION_ACTUATOR_COMMNAND
            val gson = Gson()
            val networkStr = gson.toJson(network)
            val actuatorStr = gson.toJson(actuator)
            intent.putExtra(EXTRA_NETWORK, networkStr)
            intent.putExtra(EXTRA_ACTUATOR, actuatorStr)
            intent.putExtra(EXTRA_DATATOSEND, dataToSend)
            context.startService(intent)
        }
    }
}
