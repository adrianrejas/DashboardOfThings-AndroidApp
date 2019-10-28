package com.arejas.dashboardofthings.presentation.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import com.arejas.dashboardofthings.domain.entities.widget.SensorWidgetItem;


import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsFragment;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.Utils;

/**
 * Implementation of App Widget functionality.
 */
public class SensorWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, SensorWidgetItem sensorInfo) {
        if (sensorInfo != null) {
            updateAppWidgetWithData(context, appWidgetManager, appWidgetId, sensorInfo);
        } else {
            updateAppWidgetSelectSensor(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateAppWidgetWithData(Context context, AppWidgetManager appWidgetManager,
                                               int widgetId, SensorWidgetItem sensorInfo) {
        if (sensorInfo != null) {
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sensor_widget);
            // Show data screen
            views.setViewVisibility(R.id.ll_widget_info, View.VISIBLE);
            views.setViewVisibility(R.id.rl_widget_loading, View.GONE);
            views.setViewVisibility(R.id.rl_widget_select_sensor, View.GONE);
            views.setViewVisibility(R.id.rl_widget_error_sensor, View.GONE);
            views.setViewVisibility(R.id.bt_widget_refresh_data, View.VISIBLE);
            views.setViewVisibility(R.id.bt_widget_select_sensor, View.VISIBLE);
            // Set the text data
            views.setTextViewText(R.id.tv_widget_sensor_name, sensorInfo.getSensorName());
            views.setTextViewText(R.id.tv_widget_sensor_type, context.getString(R.string.widget_sensor_type,
                    sensorInfo.getSensorType()));
            String dataToSet = Utils.getStringDataToPrint(sensorInfo.getLastValueReceived(),
                    sensorInfo.getSensorDataType(), sensorInfo.getSensorUnit());
            views.setTextViewText(R.id.tv_widget_sensor_data, dataToSet);
            // Set the pending intent for reloading data
            PendingIntent pendingIntentReloadData = PendingIntent.getBroadcast(context, widgetId*2,
                    SensorWidgetButtonBroadcast.createActionRequestReloadDataWidget(context, widgetId), 0);
            views.setOnClickPendingIntent(R.id.bt_widget_refresh_data, pendingIntentReloadData);
            // Set the pending intent for selecting sensor
            Intent intent = new Intent(context, SelectSensorForWidgetActivity.class);
            intent.putExtra(SensorDetailsFragment.SENSOR_ID, sensorInfo.getSensorId());
            PendingIntent pendingIntentLaunchSelection = PendingIntent.getActivity(context,
                    widgetId*2+1, intent, 0);
            views.setOnClickPendingIntent(R.id.bt_widget_select_sensor, pendingIntentLaunchSelection);
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(widgetId, views);
        }
    }

    public static void updateAppWidgetAsLoading(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sensor_widget);
        // Show loading screen
        views.setViewVisibility(R.id.ll_widget_info, View.GONE);
        views.setViewVisibility(R.id.rl_widget_loading, View.VISIBLE);
        views.setViewVisibility(R.id.rl_widget_select_sensor, View.GONE);
        views.setViewVisibility(R.id.rl_widget_error_sensor, View.GONE);
        views.setViewVisibility(R.id.bt_widget_refresh_data, View.GONE);
        views.setViewVisibility(R.id.bt_widget_select_sensor, View.VISIBLE);
        // Set the pending intent for selecting sensor
        Intent intent = new Intent(context, SelectSensorForWidgetActivity.class);
        intent.putExtra(SelectSensorForWidgetActivity.WIDGET_ID, widgetId);
        PendingIntent pendingIntentLaunchSelection = PendingIntent.getActivity(context,
                widgetId*2+1, intent, 0);
        views.setOnClickPendingIntent(R.id.bt_widget_select_sensor, pendingIntentLaunchSelection);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(widgetId, views);
    }

    public static void updateAppWidgetSetData(Context context, AppWidgetManager appWidgetManager, int widgetId,
                                              String lastValue, Enumerators.DataType dataType, String dataUnit) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sensor_widget);
        // Set the value text
        String dataToSet = Utils.getStringDataToPrint(lastValue,
                dataType, dataUnit);
        views.setTextViewText(R.id.tv_widget_sensor_data, dataToSet);
        // Instruct the widget manager to update the widget partially
        appWidgetManager.partiallyUpdateAppWidget(widgetId, views);
    }

    public static void updateAppWidgetError(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sensor_widget);
        // Show error screen
        views.setViewVisibility(R.id.ll_widget_info, View.GONE);
        views.setViewVisibility(R.id.rl_widget_loading, View.GONE);
        views.setViewVisibility(R.id.rl_widget_select_sensor, View.GONE);
        views.setViewVisibility(R.id.rl_widget_error_sensor, View.VISIBLE);
        views.setViewVisibility(R.id.bt_widget_refresh_data, View.GONE);
        views.setViewVisibility(R.id.bt_widget_select_sensor, View.VISIBLE);
        // Set the pending intent for selecting sensor
        Intent intent = new Intent(context, SelectSensorForWidgetActivity.class);
        intent.putExtra(SelectSensorForWidgetActivity.WIDGET_ID, widgetId);
        PendingIntent pendingIntentLaunchSelection = PendingIntent.getActivity(context,
                widgetId*2+1, intent, 0);
        views.setOnClickPendingIntent(R.id.bt_widget_select_sensor, pendingIntentLaunchSelection);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(widgetId, views);

    }

    public static void updateAppWidgetSelectSensor(Context context, AppWidgetManager appWidgetManager, int widgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.sensor_widget);
        // Show select screen
        views.setViewVisibility(R.id.ll_widget_info, View.GONE);
        views.setViewVisibility(R.id.rl_widget_loading, View.GONE);
        views.setViewVisibility(R.id.rl_widget_select_sensor, View.VISIBLE);
        views.setViewVisibility(R.id.rl_widget_error_sensor, View.GONE);
        views.setViewVisibility(R.id.bt_widget_refresh_data, View.GONE);
        views.setViewVisibility(R.id.bt_widget_select_sensor, View.VISIBLE);
        // Set the pending intent for selecting sensor
        Intent intent = new Intent(context, SelectSensorForWidgetActivity.class);
        intent.putExtra(SelectSensorForWidgetActivity.WIDGET_ID, widgetId);
        PendingIntent pendingIntentLaunchSelection = PendingIntent.getActivity(context,
                widgetId*2+1, intent, 0);
        views.setOnClickPendingIntent(R.id.bt_widget_select_sensor, pendingIntentLaunchSelection);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(widgetId, views);

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Request an update to interface service
        SensorWidgetService.startActionUpdateWidgets(context);
    }
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        // Notify the interface widget to remove widget IDs
        for (int widgetId: appWidgetIds) {
            SensorWidgetService.startActionRemoveWidget(context, widgetId);
        }
    }

}

