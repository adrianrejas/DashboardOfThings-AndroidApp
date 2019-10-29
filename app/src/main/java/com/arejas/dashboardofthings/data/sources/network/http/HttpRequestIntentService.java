package com.arejas.dashboardofthings.data.sources.network.http;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.rx.RxHelper;
import com.google.gson.Gson;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class HttpRequestIntentService extends IntentService {

    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SENSOR_REQUEST = "com.arejas.dashboardofthings.data.sources.network.http.action.SENSOR_REQUEST";
    private static final String ACTION_ACTUATOR_COMMNAND = "com.arejas.dashboardofthings.data.sources.network.http.action.ACTUATOR_COMMNAND";

    private static final String EXTRA_NETWORK = "com.arejas.dashboardofthings.data.sources.network.http.extra.NETWORK";
    private static final String EXTRA_SENSOR = "com.arejas.dashboardofthings.data.sources.network.http.extra.SENSOR";
    private static final String EXTRA_ACTUATOR = "com.arejas.dashboardofthings.data.sources.network.http.extra.ACTUATOR";
    private static final String EXTRA_DATATOSEND = "com.arejas.dashboardofthings.data.sources.network.http.extra.DATATOSEND";

    public HttpRequestIntentService() {
        super("HttpRequestIntentService");
    }

    /**
     * Starts this service to perform action SENSOR REQUEST with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSensorRequest(Context context, Network network, Sensor sensor) {
        Intent intent = new Intent(context, HttpRequestIntentService.class);
        intent.setAction(ACTION_SENSOR_REQUEST);
        Gson gson = new Gson();
        String networkStr = gson.toJson(network);
        String sensorStr = gson.toJson(sensor);
        intent.putExtra(EXTRA_NETWORK, networkStr);
        intent.putExtra(EXTRA_SENSOR, sensorStr);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action ACTUATOR COMMAND with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionActuatorCommand(Context context,  Network network, Actuator actuator, String dataToSend) {
        Intent intent = new Intent(context, HttpRequestIntentService.class);
        intent.setAction(ACTION_ACTUATOR_COMMNAND);
        Gson gson = new Gson();
        String networkStr = gson.toJson(network);
        String actuatorStr = gson.toJson(actuator);
        intent.putExtra(EXTRA_NETWORK, networkStr);
        intent.putExtra(EXTRA_ACTUATOR, actuatorStr);
        intent.putExtra(EXTRA_DATATOSEND, dataToSend);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            if (intent != null) {
                final String action = intent.getAction();
                if (ACTION_SENSOR_REQUEST.equals(action)) {
                    Gson gson = new Gson();
                    String networkStr = intent.getExtras().getString(EXTRA_NETWORK);
                    String sensorStr = intent.getExtras().getString(EXTRA_SENSOR);
                    Network network = gson.fromJson(networkStr, Network.class);
                    Sensor sensor = gson.fromJson(sensorStr, Sensor.class);
                    handleActionSensorRequest(network, sensor);
                } else if (ACTION_ACTUATOR_COMMNAND.equals(action)) {
                    Gson gson = new Gson();
                    String networkStr = intent.getExtras().getString(EXTRA_NETWORK);
                    String actuatorStr = intent.getExtras().getString(EXTRA_ACTUATOR);
                    final Network network = gson.fromJson(networkStr, Network.class);
                    final Actuator actuator = gson.fromJson(actuatorStr, Actuator.class);
                    final String dataToSend = intent.getStringExtra(EXTRA_DATATOSEND);
                    handleActionActuatorCommand(network, actuator, dataToSend);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle action Sensor request in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSensorRequest(Network network, Sensor sensor) {
        try {
            HttpRequestHelper.sendSensorDataHttpRequest(getApplicationContext(), network, sensor);
        } catch (Exception e) {
            RxHelper.publishLog(network.getId(), Enumerators.ElementType.NETWORK,
                    network.getName(), Enumerators.LogLevel.ERROR,
                    getString(R.string.log_critical_unexpected_http_network));
        }
    }

    /**
     * Handle action Actuator command in the provided background thread with the provided
     * parameters.
     */
    private void handleActionActuatorCommand(Network network, Actuator actuator, String dataToSend) {
        try {
            HttpRequestHelper.sendActuatorCommand(getApplicationContext(), network, actuator, dataToSend);
        } catch (Exception e) {
            RxHelper.publishLog(network.getId(), Enumerators.ElementType.NETWORK,
                    network.getName(), Enumerators.LogLevel.ERROR,
                    getString(R.string.log_critical_unexpected_http_network));
        }
    }
}
