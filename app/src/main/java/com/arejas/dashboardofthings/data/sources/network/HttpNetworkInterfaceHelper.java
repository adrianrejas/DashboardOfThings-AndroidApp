package com.arejas.dashboardofthings.data.sources.network;

import android.content.Context;
import android.os.Bundle;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.data.sources.network.http.HttpRequestIntentService;
import com.arejas.dashboardofthings.data.sources.network.http.HttpSensorRequestJobService;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.rx.RxHelper;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.google.gson.Gson;

public class HttpNetworkInterfaceHelper extends NetworkInterfaceHelper{

    private static final int MARGIN_WINDOW_PERIODIC_SECONDS = 30;

    private FirebaseJobDispatcher dispatcher;

    public HttpNetworkInterfaceHelper(Network network) {
        super(network);
    }

    @Override
    public boolean initNetworkInterface(Context context, Sensor[] sensors) {
        // Create a new dispatcher using the Google Play driver.
        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        for (Sensor sensor: sensors) {
            configureSensorReceiving(context, sensor);
        }
        return true;
    }

    @Override
    public boolean closeNetworkInterface(Context context) {
        // cancell all jobs and set the job dispatcher to null
        dispatcher.cancelAll();
        dispatcher = null;
        return true;
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
    @Override
    public boolean configureSensorReceiving(Context context, Sensor sensor) {
        try {
            Bundle extras = new Bundle();
            Gson gson = new Gson();
            String networkStr = gson.toJson(getNetwork());
            String sensorStr = gson.toJson(sensor);
            extras.putString(HttpSensorRequestJobService.NETWORK_OBJECT, networkStr);
            extras.putString(HttpSensorRequestJobService.SENSOR_OBJECT, sensorStr);
            Job sensorJob = dispatcher.newJobBuilder()
                    .setService(HttpSensorRequestJobService.class)
                    .setTag(Integer.toString(sensor.getId()))
                    .setRecurring(true)
                    .setLifetime(Lifetime.UNTIL_NEXT_BOOT)
                    .setTrigger(Trigger.executionWindow(sensor.getHttpSecondsBetweenRequests(),
                            sensor.getHttpSecondsBetweenRequests() + MARGIN_WINDOW_PERIODIC_SECONDS))
                    .setReplaceCurrent(true)
                    .setExtras(extras)
                    .build();
            dispatcher.mustSchedule(sensorJob);
            registerSensor(sensor);
            return true;
        } catch (Exception e) {
            RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                    sensor.getName(), Enumerators.LogLevel.ERROR,
                    context.getString(R.string.log_critical_sensor_scheduling));
            return false;
        }
    }

    @Override
    public boolean unconfigureSensorReceiving(Context context, Sensor sensor) {
        unregisterSensor(sensor);
        dispatcher.cancel(Integer.toString(sensor.getId()));
        return true;
    }

    @Override
    public boolean sendActuatorData(Context context, Actuator actuator, String dataToSend) {
        try {
            HttpRequestIntentService.startActionActuatorCommand(context, getNetwork(), actuator, dataToSend);
            return true;
        } catch (Exception e) {
            RxHelper.publishLog(actuator.getId(), Enumerators.ElementType.ACTUATOR,
                    actuator.getName(), Enumerators.LogLevel.ERROR,
                    context.getString(R.string.log_critical_actautor_send));
            return false;
        }
    }

    @Override
    public boolean requestSensorReload(Context context, Sensor sensor) {
        try {
            HttpRequestIntentService.startActionSensorRequest(context, getNetwork(), sensor);
            return true;
        } catch (Exception e) {
            RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                    sensor.getName(), Enumerators.LogLevel.ERROR,
                    context.getString(R.string.log_critical_sensor_scheduling));
            return false;
        }
    }

}
