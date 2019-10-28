package com.arejas.dashboardofthings.presentation.ui.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.entities.widget.SensorWidgetItem;
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SensorWidgetService extends JobIntentService {

    public static final int UNKNOWN_ELEMENT_ID = -1;
    private static final String WIDGET_PREFERENCES = "widgetPreferences";

    private static final int JOB_ID_UPDATE = 1;
    private static final int JOB_ID_SET_SENSOR = 1;
    private static final int JOB_ID_REMOVE_WIDGET = 1;

    private static final String ACTION_UPDATE_WIDGETS = "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_UPDATE_WIDGETS";
    private static final String ACTION_UPDATE_WIDGET = "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_UPDATE_WIDGET";
    private static final String ACTION_UPDATE_WIDGET_SET_DATA = "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_UPDATE_WIDGET_SET_DATA";
    private static final String ACTION_SET_SENSOR_FOR_WIDGET = "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_SET_SENSOR_FOR_WIDGET";
    private static final String ACTION_REQUEST_RELOAD_DATA_WIDGET = "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_REQUEST_RELOAD_DATA_WIDGET";
    private static final String ACTION_REMOVE_WIDGET = "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_REMOVE_WIDGET";

    private static final String EXTRA_WIDGET_ID = "WIDGET_ID";
    private static final String EXTRA_SENSOR_ID = "SENSOR_ID";
    private static final String EXTRA_VALUE = "VALUE";

    @Inject
    public SensorManagementUseCase sensorManagementUseCase;

    public SensorWidgetService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Dependency injection
        AndroidInjection.inject(this);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_WIDGETS.equals(action)) {
                handleActionUpdateWidgets();
            } else if (ACTION_UPDATE_WIDGET.equals(action)) {
                final int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
                final int sensorId = intent.getIntExtra(EXTRA_SENSOR_ID, UNKNOWN_ELEMENT_ID);
                handleActionSetSensorForWidget(widgetId, sensorId);
            }  else if (ACTION_UPDATE_WIDGET_SET_DATA.equals(action)) {
                final int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
                final String value = intent.getStringExtra(EXTRA_VALUE);
                handleActionUpdateWidgetSetData(widgetId, value);
            } else if (ACTION_SET_SENSOR_FOR_WIDGET.equals(action)) {
                final int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
                final int sensorId = intent.getIntExtra(EXTRA_SENSOR_ID, UNKNOWN_ELEMENT_ID);
                handleActionSetSensorForWidget(widgetId, sensorId);
            } else if (ACTION_REQUEST_RELOAD_DATA_WIDGET.equals(action)) {
                final int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
                handleActionRequestReloadData(widgetId);
            } else if (ACTION_REMOVE_WIDGET.equals(action)) {
                final int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0);
                handleActionRemoveWidget(widgetId);
            }
        }
    }

    /**
     * Handle action UpdateWidgets
     */
    private void handleActionUpdateWidgets() {
        // Get app widget manager and the IDs of the widget instances deployed
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, SensorWidget.class));
        // For each of the widget instances
        Map<Integer, Integer> sensorIdToWidgetIdMap = new HashMap<>();
        for (int widgetId: appWidgetIds) {
            // Get recipe Id for widget from preferences
            int sensorId = getSensorIdForWidgetId(this, widgetId);
            // if sensor id selected, put in a map for processing it
            // if not, launch the update of the widget for the selection of sensor
            if (sensorId != UNKNOWN_ELEMENT_ID) {
                sensorIdToWidgetIdMap.put(sensorId, widgetId);
            } else {
                SensorWidget.updateAppWidgetSelectSensor(getApplicationContext(), appWidgetManager, widgetId);
            }
        }
        Set<Integer> keys = sensorIdToWidgetIdMap.keySet();
        int[] sensorIds = new int[keys.size()];
        int i = 0;
        for (Integer sensorId : keys) {
            sensorIds[i] = sensorId;
            i++;
        }
        sensorManagementUseCase.getSensorInfoForWidgets(sensorIds ,dataList -> {
            //Now update all the widgets
            if (dataList != null) {
                for (SensorWidgetItem sensorInfo : dataList) {
                    int widgetId = sensorIdToWidgetIdMap.get(sensorInfo.getSensorId());
                    saveUnitUsedForWidgetId(getApplicationContext(), widgetId, sensorInfo.getSensorUnit());
                    SensorWidget.updateAppWidget(getApplicationContext(), appWidgetManager, widgetId, sensorInfo);
                }
            }
        });
    }

    /**
     * Handle action UpdateWidgets
     */
    private void handleActionUpdateWidget(int widgetId) {
        // Get app widget manager and the IDs of the widget instances deployed
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        final int sensorId = getSensorIdForWidgetId(this, widgetId);
        if (sensorId != UNKNOWN_ELEMENT_ID) {
            sensorManagementUseCase.getSensorInfoForWidget(sensorId, sensorInfo -> {
                //Now update the widget
                if (sensorInfo != null) {
                    SensorWidget.updateAppWidget(getApplicationContext(), appWidgetManager, widgetId, sensorInfo);
                    saveUnitUsedForWidgetId(getApplicationContext(), widgetId, sensorInfo.getSensorUnit());
                    saveDataTypeUsedForWidgetId(getApplicationContext(), widgetId, sensorInfo.getSensorDataType().ordinal());
                }
            });
        }
    }

    /**
     * Handle action SetSensorForWidget in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSetSensorForWidget(int widgetId, int sensorId) {
        // get app widget manager
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        // Save preferences for widget Id
        saveSensorIdForWidgetId(getApplicationContext(), widgetId, sensorId);
        //Set now widget as loading
        SensorWidget.updateAppWidgetAsLoading(this, appWidgetManager, widgetId);
        // Request data of the new recipe and update the widget with it
        sensorManagementUseCase.getSensorInfoForWidget(sensorId ,sensorInfo -> {
            //Now update the widget
            if (sensorInfo != null) {
                SensorWidget.updateAppWidget(getApplicationContext(), appWidgetManager, widgetId, sensorInfo);
                saveUnitUsedForWidgetId(getApplicationContext(), widgetId, sensorInfo.getSensorUnit());
                saveDataTypeUsedForWidgetId(getApplicationContext(), widgetId, sensorInfo.getSensorDataType().ordinal());
            }
        });
    }

    /**
     * Handle action UpdateWidgetSetData in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUpdateWidgetSetData(int widgetId, String value) {
        // get app widget manager
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        if (value != null) {
            String unit = getUnitUsedForWidgetId(getApplicationContext(), widgetId);
            Enumerators.DataType dataType = null;
            int dataTypeInt = getDataTypeUsedForWidgetId(getApplicationContext(), widgetId);
            if (dataTypeInt != UNKNOWN_ELEMENT_ID) {
                dataType = Enumerators.DataType.valueOf(dataTypeInt);
            }
            SensorWidget.updateAppWidgetSetData(getApplicationContext(), appWidgetManager, widgetId, value, dataType, unit);
        }
    }

    /**
     * Handle action RequestReloadData in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRequestReloadData(int widgetId) {
        // get app widget manager
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int sensorId = getSensorIdForWidgetId(getApplicationContext(), widgetId);
        sensorManagementUseCase.requestSensorReload(sensorId);
        if (sensorId != UNKNOWN_ELEMENT_ID) {
            sensorManagementUseCase.requestSensorReload(sensorId);
        }
    }

    /**
     * Handle action RemoveWidget in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRemoveWidget(int widgetId) {
        // Remove preferences for widget Id
        removeSensorIdForWidgetId(getApplicationContext(), widgetId);
        removeUnitUsedForWidgetId(getApplicationContext(), widgetId);
        removeDataTypeUsedForWidgetId(getApplicationContext(), widgetId);
    }

    /**
     * Starts this service to perform action UpdateWidgets with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateWidgets(Context context) {
        Intent intent = new Intent(context, SensorWidgetService.class);
        intent.setAction(ACTION_UPDATE_WIDGETS);
        enqueueWork(context, SensorWidgetService.class, JOB_ID_UPDATE, intent);
    }

    /**
     * Starts this service to perform action UpdateWidget with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateWidget(Context context, int widgetId) {
        Intent intent = new Intent(context, SensorWidgetService.class);
        intent.setAction(ACTION_UPDATE_WIDGET);
        intent.putExtra(EXTRA_WIDGET_ID, widgetId);
        enqueueWork(context, SensorWidgetService.class, JOB_ID_UPDATE, intent);
    }

    /**
     * Starts this service to perform action UpdateWidgetSetData with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdateWidgetSetData(Context context, int widgetId, String value) {
        Intent intent = new Intent(context, SensorWidgetService.class);
        intent.setAction(ACTION_UPDATE_WIDGET_SET_DATA);
        intent.putExtra(EXTRA_WIDGET_ID, widgetId);
        intent.putExtra(EXTRA_VALUE, value);
        enqueueWork(context, SensorWidgetService.class, JOB_ID_UPDATE, intent);
    }

    /**
     * Starts this service to perform action SetRecipeForWidget with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSetRecipeForWidget(Context context, int widgetId, int sensorId) {
        Intent intent = new Intent(context, SensorWidgetService.class);
        intent.setAction(ACTION_SET_SENSOR_FOR_WIDGET);
        intent.putExtra(EXTRA_WIDGET_ID, widgetId);
        intent.putExtra(EXTRA_SENSOR_ID, sensorId);
        enqueueWork(context, SensorWidgetService.class, JOB_ID_SET_SENSOR, intent);
    }

    /**
     * Starts this service to perform action RequestReloadData with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRequestReloadDataWidget(Context context, int widgetId) {
        Intent intent = new Intent(context, SensorWidgetService.class);
        intent.setAction(ACTION_REQUEST_RELOAD_DATA_WIDGET);
        intent.putExtra(EXTRA_WIDGET_ID, widgetId);
        enqueueWork(context, SensorWidgetService.class, JOB_ID_SET_SENSOR, intent);
    }

    /**
     * Starts this service to perform action RemoveWidget with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionRemoveWidget(Context context, int widgetId) {
        Intent intent = new Intent(context, SensorWidgetService.class);
        intent.setAction(ACTION_REMOVE_WIDGET);
        intent.putExtra(EXTRA_WIDGET_ID, widgetId);
        enqueueWork(context, SensorWidgetService.class, JOB_ID_REMOVE_WIDGET, intent);
    }

    public static int getWidgetIdForSensorId(Context context, int sensorId) {
        return context.getSharedPreferences(WIDGET_PREFERENCES, 0)
                .getInt(context.getString(R.string.preference_widget_id_for_sensor, sensorId),
                        UNKNOWN_ELEMENT_ID);
    }

    private static int getSensorIdForWidgetId(Context context, int widgetId) {
        return context.getSharedPreferences(WIDGET_PREFERENCES, 0)
                .getInt(context.getString(R.string.preference_sensor_id_for_widget, widgetId),
                        UNKNOWN_ELEMENT_ID);
    }

    private static void saveSensorIdForWidgetId(Context context, int widgetId, int sensorId) {
        SharedPreferences.Editor editor = context.getSharedPreferences(WIDGET_PREFERENCES, 0).edit();
        editor.putInt(context.getString(R.string.preference_sensor_id_for_widget, widgetId), sensorId);
        editor.putInt(context.getString(R.string.preference_widget_id_for_sensor, sensorId), widgetId);
        editor.apply();
        editor.clear();
    }

    private static void removeSensorIdForWidgetId(Context context, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(WIDGET_PREFERENCES, 0);
        int sensorId = prefs.getInt(context.getString(R.string.preference_sensor_id_for_widget, widgetId),
                UNKNOWN_ELEMENT_ID);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(context.getString(R.string.preference_sensor_id_for_widget, widgetId));
        if (sensorId != UNKNOWN_ELEMENT_ID)
            editor.remove(context.getString(R.string.preference_widget_id_for_sensor, sensorId));
        editor.apply();
        editor.clear();
    }

    private static String getUnitUsedForWidgetId(Context context, int widgetId) {
        return context.getSharedPreferences(WIDGET_PREFERENCES, 0)
                .getString(context.getString(R.string.preference_unit_used_for_widget, widgetId),
                        null);
    }

    private static void saveUnitUsedForWidgetId(Context context, int widgetId, String unitUsed) {
        if (unitUsed != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(WIDGET_PREFERENCES, 0).edit();
            editor.putString(context.getString(R.string.preference_unit_used_for_widget, widgetId), unitUsed);
            editor.apply();
            editor.clear();
        }
    }

    private static void removeUnitUsedForWidgetId(Context context, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(WIDGET_PREFERENCES, 0);
        String unitUsed = prefs.getString(context.getString(R.string.preference_unit_used_for_widget, widgetId),
                null);
        if (unitUsed != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(context.getString(R.string.preference_unit_used_for_widget, widgetId));
            editor.apply();
            editor.clear();
        }
    }

    private static int getDataTypeUsedForWidgetId(Context context, int widgetId) {
        return context.getSharedPreferences(WIDGET_PREFERENCES, 0)
                .getInt(context.getString(R.string.preference_data_type_used_for_widget, widgetId),
                        UNKNOWN_ELEMENT_ID);
    }

    private static void saveDataTypeUsedForWidgetId(Context context, int widgetId, int dataTypeUsed) {
        if (dataTypeUsed != UNKNOWN_ELEMENT_ID) {
            SharedPreferences.Editor editor = context.getSharedPreferences(WIDGET_PREFERENCES, 0).edit();
            editor.putInt(context.getString(R.string.preference_data_type_used_for_widget, widgetId), dataTypeUsed);
            editor.apply();
            editor.clear();
        }
    }

    private static void removeDataTypeUsedForWidgetId(Context context, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(WIDGET_PREFERENCES, 0);
        int dataTypeUsed = prefs.getInt(context.getString(R.string.preference_data_type_used_for_widget, widgetId),
                UNKNOWN_ELEMENT_ID);
        if (dataTypeUsed != UNKNOWN_ELEMENT_ID) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(context.getString(R.string.preference_data_type_used_for_widget, widgetId));
            editor.apply();
            editor.clear();
        }
    }

}
