package com.arejas.dashboardofthings.data.sources.network.http;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.domain.entities.Actuator;
import com.arejas.dashboardofthings.domain.entities.Network;
import com.arejas.dashboardofthings.domain.entities.Sensor;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.rx.RxHelper;

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
        intent.putExtra(EXTRA_NETWORK, network);
        intent.putExtra(EXTRA_SENSOR, sensor);
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
        intent.putExtra(EXTRA_NETWORK, network);
        intent.putExtra(EXTRA_ACTUATOR, actuator);
        intent.putExtra(EXTRA_DATATOSEND, dataToSend);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SENSOR_REQUEST.equals(action)) {
                final Network network = intent.getParcelableExtra(EXTRA_NETWORK);
                final Sensor sensor = intent.getParcelableExtra(EXTRA_SENSOR);
                handleActionSensorRequest(network, sensor);
            } else if (ACTION_ACTUATOR_COMMNAND.equals(action)) {
                final Network network = intent.getParcelableExtra(EXTRA_NETWORK);
                final Actuator actuator = intent.getParcelableExtra(EXTRA_ACTUATOR);
                final String dataToSend = intent.getStringExtra(EXTRA_DATATOSEND);
                handleActionActuatorCommand(network, actuator, dataToSend);
            }
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
                    Enumerators.LogLevel.CRITICAL,
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
                    Enumerators.LogLevel.CRITICAL,
                    getString(R.string.log_critical_unexpected_http_network));
        }
    }
}
