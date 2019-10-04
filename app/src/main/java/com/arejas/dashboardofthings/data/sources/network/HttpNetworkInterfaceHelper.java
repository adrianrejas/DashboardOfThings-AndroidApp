package com.arejas.dashboardofthings.data.sources.network;

import android.content.Context;
import android.os.Bundle;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.data.sources.network.http.HttpRequestIntentService;
import com.arejas.dashboardofthings.data.sources.network.http.HttpSensorRequestJobService;
import com.arejas.dashboardofthings.domain.entities.Actuator;
import com.arejas.dashboardofthings.domain.entities.Network;
import com.arejas.dashboardofthings.domain.entities.Sensor;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.rx.RxHelper;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;

public class HttpNetworkInterfaceHelper extends NetworkInterfaceHelper{

    private static final int MARGIN_WINDOW_PERIODIC_SECONDS = 15;

    private FirebaseJobDispatcher dispatcher;

    public HttpNetworkInterfaceHelper(Network network) {
        super(network);
    }

    @Override
    public void initNetworkInterface(Context context, Sensor[] sensors) {
        // Create a new dispatcher using the Google Play driver.
        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        for (Sensor sensor: sensors) {
            configureSensorReceiving(context, sensor);
        }
    }

    @Override
    public void closeNetworkInterface(Context context) {
        // cancell all jobs and set the job dispatcher to null
        dispatcher.cancelAll();
        dispatcher = null;
    }

    @Override
    public void configureSensorReceiving(Context context, Sensor sensor) {
        try {
            Bundle extras = new Bundle();
            extras.putParcelable(HttpSensorRequestJobService.NETWORK_OBJECT, getNetwork());
            extras.putParcelable(HttpSensorRequestJobService.SENSOR_OBJECT, sensor);
            Job sensorJob = dispatcher.newJobBuilder()
                    .setService(HttpSensorRequestJobService.class)  // the JobService that will be called
                    .setTag(Integer.toString(sensor.getId()))       // uniquely identifies the job
                    .setRecurring(false)                            // recurring job
                    .setLifetime(Lifetime.UNTIL_NEXT_BOOT)          // don't persist past a device reboot
                    // start between the configured seconds and an additional margin
                    .setTrigger(Trigger.executionWindow(sensor.getHttpSecondsBetweenRequests(),
                            sensor.getHttpSecondsBetweenRequests() + MARGIN_WINDOW_PERIODIC_SECONDS))
                    .setReplaceCurrent(true)                       // Overwrite an existing job with the same tag
                    .setRetryStrategy(dispatcher.newRetryStrategy(RetryStrategy.RETRY_POLICY_LINEAR,    // retry with linear backoff
                            sensor.getHttpSecondsBetweenRequests(),
                            sensor.getHttpSecondsBetweenRequests() + MARGIN_WINDOW_PERIODIC_SECONDS))
                    .setExtras(extras)                              //Send the extras required for the service
                    .build();
            dispatcher.mustSchedule(sensorJob);
        } catch (Exception e) {
            RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                    Enumerators.LogLevel.CRITICAL,
                    context.getString(R.string.log_critical_sensor_scheduling));
        }
    }

    public void unconfigureSensorReceiving(Context context, Sensor sensor) {
        dispatcher.cancel(Integer.toString(sensor.getId()));
    }

    @Override
    public void sendActuatorData(Context context, Actuator actuator, String dataToSend) {
        try {
            HttpRequestIntentService.startActionActuatorCommand(context, getNetwork(), actuator, dataToSend);
        } catch (Exception e) {
            RxHelper.publishLog(actuator.getId(), Enumerators.ElementType.ACTUATOR,
                    Enumerators.LogLevel.CRITICAL,
                    context.getString(R.string.log_critical_sensor_scheduling));
        }
    }

}
