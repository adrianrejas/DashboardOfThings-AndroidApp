package com.arejas.dashboardofthings.presentation.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.arejas.dashboardofthings.domain.entities.widget.SensorWidgetItem


import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsFragment
import com.arejas.dashboardofthings.utils.Enumerators
import com.arejas.dashboardofthings.utils.Utils

/**
 * Implementation of App Widget functionality.
 */
class SensorWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Request an update to interface service
        SensorWidgetService.startActionUpdateWidgets(context)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        // Notify the interface widget to remove widget IDs
        for (widgetId in appWidgetIds) {
            SensorWidgetService.startActionRemoveWidget(context, widgetId)
        }
    }

    companion object {

        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int, sensorInfo: SensorWidgetItem?
        ) {
            if (sensorInfo != null) {
                updateAppWidgetWithData(context, appWidgetManager, appWidgetId, sensorInfo)
            } else {
                updateAppWidgetSelectSensor(context, appWidgetManager, appWidgetId)
            }
        }

        fun updateAppWidgetWithData(
            context: Context, appWidgetManager: AppWidgetManager,
            widgetId: Int, sensorInfo: SensorWidgetItem?
        ) {
            if (sensorInfo != null) {
                // Construct the RemoteViews object
                val views = RemoteViews(context.packageName, R.layout.sensor_widget)
                // Show data screen
                views.setViewVisibility(R.id.ll_widget_info, View.VISIBLE)
                views.setViewVisibility(R.id.rl_widget_loading, View.GONE)
                views.setViewVisibility(R.id.rl_widget_select_sensor, View.GONE)
                views.setViewVisibility(R.id.rl_widget_error_sensor, View.GONE)
                views.setViewVisibility(R.id.bt_widget_refresh_data, View.VISIBLE)
                views.setViewVisibility(R.id.bt_widget_select_sensor, View.VISIBLE)
                // Set the text data
                views.setTextViewText(R.id.tv_widget_sensor_name, sensorInfo.sensorName)
                views.setTextViewText(
                    R.id.tv_widget_sensor_type, context.getString(
                        R.string.widget_sensor_type,
                        sensorInfo.sensorType
                    )
                )
                val dataToSet = Utils.getStringDataToPrint(
                    sensorInfo.lastValueReceived,
                    sensorInfo.sensorDataType, sensorInfo.sensorUnit
                )
                views.setTextViewText(R.id.tv_widget_sensor_data, dataToSet)
                // Set the pending intent for reloading data
                val pendingIntentReloadData = PendingIntent.getBroadcast(
                    context, widgetId * 2,
                    SensorWidgetButtonBroadcast.createActionRequestReloadDataWidget(
                        context,
                        widgetId
                    ), 0
                )
                views.setOnClickPendingIntent(R.id.bt_widget_refresh_data, pendingIntentReloadData)
                // Set the pending intent for selecting sensor
                val intent = Intent(context, SelectSensorForWidgetActivity::class.java)
                intent.putExtra(SensorDetailsFragment.SENSOR_ID, sensorInfo.sensorId)
                val pendingIntentLaunchSelection = PendingIntent.getActivity(
                    context,
                    widgetId * 2 + 1, intent, 0
                )
                views.setOnClickPendingIntent(
                    R.id.bt_widget_select_sensor,
                    pendingIntentLaunchSelection
                )
                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }

        fun updateAppWidgetAsLoading(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.sensor_widget)
            // Show loading screen
            views.setViewVisibility(R.id.ll_widget_info, View.GONE)
            views.setViewVisibility(R.id.rl_widget_loading, View.VISIBLE)
            views.setViewVisibility(R.id.rl_widget_select_sensor, View.GONE)
            views.setViewVisibility(R.id.rl_widget_error_sensor, View.GONE)
            views.setViewVisibility(R.id.bt_widget_refresh_data, View.GONE)
            views.setViewVisibility(R.id.bt_widget_select_sensor, View.VISIBLE)
            // Set the pending intent for selecting sensor
            val intent = Intent(context, SelectSensorForWidgetActivity::class.java)
            intent.putExtra(SelectSensorForWidgetActivity.WIDGET_ID, widgetId)
            val pendingIntentLaunchSelection = PendingIntent.getActivity(
                context,
                widgetId * 2 + 1, intent, 0
            )
            views.setOnClickPendingIntent(
                R.id.bt_widget_select_sensor,
                pendingIntentLaunchSelection
            )
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(widgetId, views)
        }

        fun updateAppWidgetSetData(
            context: Context, appWidgetManager: AppWidgetManager, widgetId: Int,
            lastValue: String, dataType: Enumerators.DataType, dataUnit: String
        ) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.sensor_widget)
            // Set the value text
            val dataToSet = Utils.getStringDataToPrint(
                lastValue,
                dataType, dataUnit
            )
            views.setTextViewText(R.id.tv_widget_sensor_data, dataToSet)
            // Instruct the widget manager to update the widget partially
            appWidgetManager.partiallyUpdateAppWidget(widgetId, views)
        }

        fun updateAppWidgetError(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.sensor_widget)
            // Show error screen
            views.setViewVisibility(R.id.ll_widget_info, View.GONE)
            views.setViewVisibility(R.id.rl_widget_loading, View.GONE)
            views.setViewVisibility(R.id.rl_widget_select_sensor, View.GONE)
            views.setViewVisibility(R.id.rl_widget_error_sensor, View.VISIBLE)
            views.setViewVisibility(R.id.bt_widget_refresh_data, View.GONE)
            views.setViewVisibility(R.id.bt_widget_select_sensor, View.VISIBLE)
            // Set the pending intent for selecting sensor
            val intent = Intent(context, SelectSensorForWidgetActivity::class.java)
            intent.putExtra(SelectSensorForWidgetActivity.WIDGET_ID, widgetId)
            val pendingIntentLaunchSelection = PendingIntent.getActivity(
                context,
                widgetId * 2 + 1, intent, 0
            )
            views.setOnClickPendingIntent(
                R.id.bt_widget_select_sensor,
                pendingIntentLaunchSelection
            )
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(widgetId, views)

        }

        fun updateAppWidgetSelectSensor(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.sensor_widget)
            // Show select screen
            views.setViewVisibility(R.id.ll_widget_info, View.GONE)
            views.setViewVisibility(R.id.rl_widget_loading, View.GONE)
            views.setViewVisibility(R.id.rl_widget_select_sensor, View.VISIBLE)
            views.setViewVisibility(R.id.rl_widget_error_sensor, View.GONE)
            views.setViewVisibility(R.id.bt_widget_refresh_data, View.GONE)
            views.setViewVisibility(R.id.bt_widget_select_sensor, View.VISIBLE)
            // Set the pending intent for selecting sensor
            val intent = Intent(context, SelectSensorForWidgetActivity::class.java)
            intent.putExtra(SelectSensorForWidgetActivity.WIDGET_ID, widgetId)
            val pendingIntentLaunchSelection = PendingIntent.getActivity(
                context,
                widgetId * 2 + 1, intent, 0
            )
            views.setOnClickPendingIntent(
                R.id.bt_widget_select_sensor,
                pendingIntentLaunchSelection
            )
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(widgetId, views)

        }
    }

}

