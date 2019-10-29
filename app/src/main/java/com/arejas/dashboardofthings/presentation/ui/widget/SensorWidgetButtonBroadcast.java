package com.arejas.dashboardofthings.presentation.ui.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SensorWidgetButtonBroadcast extends BroadcastReceiver {

    private static final String ACTION_REQUEST_SENSOR_RELOAD = "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_REQUEST_SENSOR_RELOAD";
    private static final String ACTION_REQUEST_SELECT_SENSOR = "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_REQUEST_SELECT_SENSOR";

    private static final String EXTRA_WIDGET_ID = "com.arejas.dashboardofthings.presentation.ui.widget.WIDGET_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_REQUEST_SENSOR_RELOAD.equals(action)) {
                final int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, SensorWidgetService.UNKNOWN_ELEMENT_ID);
                if (widgetId != SensorWidgetService.UNKNOWN_ELEMENT_ID) {
                    SensorWidgetService.startActionRequestReloadDataWidget(context, widgetId);
                }
            } else if (ACTION_REQUEST_SELECT_SENSOR.equals(action)) {
                final int widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, SensorWidgetService.UNKNOWN_ELEMENT_ID);
                if (widgetId != SensorWidgetService.UNKNOWN_ELEMENT_ID) {
                    Intent activityIntent = new Intent(context, SelectSensorForWidgetActivity.class);
                    activityIntent.putExtra(SelectSensorForWidgetActivity.WIDGET_ID, widgetId);
                    context.startActivity(activityIntent);
                }
            }
        }
    }

    public static Intent createActionSelectSensorWidget(Context context, int widgetId) {
        Intent intent = new Intent(context, SensorWidgetButtonBroadcast.class);
        intent.setAction(ACTION_REQUEST_SELECT_SENSOR);
        intent.putExtra(EXTRA_WIDGET_ID, widgetId);
        return intent;
    }

    public static Intent createActionRequestReloadDataWidget(Context context, int widgetId) {
        Intent intent = new Intent(context, SensorWidgetButtonBroadcast.class);
        intent.setAction(ACTION_REQUEST_SENSOR_RELOAD);
        intent.putExtra(EXTRA_WIDGET_ID, widgetId);
        return intent;
    }
}
