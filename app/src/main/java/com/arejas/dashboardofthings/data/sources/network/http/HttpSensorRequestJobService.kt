package com.arejas.dashboardofthings.data.sources.network.http


import android.util.Log

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.utils.Enumerators
import com.arejas.dashboardofthings.utils.rx.RxHelper
import com.firebase.jobdispatcher.JobParameters
import com.firebase.jobdispatcher.JobService
import com.google.gson.Gson

class HttpSensorRequestJobService : JobService() {

    override fun onStartJob(jobParameters: JobParameters): Boolean {
        try {
            val gson = Gson()
            val networkStr = jobParameters.extras!!.getString(NETWORK_OBJECT)
            val sensorStr = jobParameters.extras!!.getString(SENSOR_OBJECT)
            val network = gson.fromJson(networkStr, Network::class.java)
            val sensor = gson.fromJson(sensorStr, Sensor::class.java)
            HttpRequestIntentService.startActionSensorRequest(
                applicationContext,
                network, sensor
            )
            Log.d("SENSOR", "ACTUAIZANDO SENSOR " + sensor.id!!)
            return false
        } catch (e: Exception) {
            RxHelper.publishLog(
                0, Enumerators.ElementType.NETWORK,
                null, Enumerators.LogLevel.ERROR,
                getString(R.string.log_critical_sensor_scheduling)
            )
            return false
        }

    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        return false
    }

    companion object {

        //TODO
        /* For passing the database parameters to the job service, right now they are converted in
     * JSON strings and decoded again when received. This is because using the database entities
     * as parcelable objects caused problems in some of the devices tested. For now, we're using GSON
     * for parsing because, although less optimized, is working on every situation. But I'll keep up
     * researching on this.*/

        val NETWORK_OBJECT = "NETWORK_OBJECT"
        val SENSOR_OBJECT = "SENSOR_OBJECT"
    }

}
