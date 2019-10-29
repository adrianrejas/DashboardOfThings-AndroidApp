package com.arejas.dashboardofthings.data.sources.network.http;


import android.util.Log;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.rx.RxHelper;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.gson.Gson;

public class HttpSensorRequestJobService extends JobService {

    //TODO
    /* For passing the database parameters to the job service, right now they are converted in
     * JSON strings and decoded again when received. This is because using the database entities
     * as parcelable objects caused problems in some of the devices tested. For now, we're using GSON
     * for parsing because, although less optimized, is working on every situation. But I'll keep up
     * researching on this.*/

    public static final String NETWORK_OBJECT = "NETWORK_OBJECT";
    public static final String SENSOR_OBJECT = "SENSOR_OBJECT";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        try {
            Gson gson = new Gson();
            String networkStr = jobParameters.getExtras().getString(NETWORK_OBJECT);
            String sensorStr = jobParameters.getExtras().getString(SENSOR_OBJECT);
            Network network = gson.fromJson(networkStr, Network.class);
            Sensor sensor = gson.fromJson(sensorStr, Sensor.class);
            HttpRequestIntentService.startActionSensorRequest(getApplicationContext(),
                    network, sensor);
            Log.d("SENSOR", "ACTUAIZANDO SENSOR " + sensor.getId());
            return false;
        } catch (Exception e) {
            RxHelper.publishLog(0, Enumerators.ElementType.NETWORK,
                    null, Enumerators.LogLevel.ERROR,
                    getString(R.string.log_critical_sensor_scheduling));
            return false;
        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
