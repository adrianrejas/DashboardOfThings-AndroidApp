package com.arejas.dashboardofthings.presentation.ui.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SensorWidgetButtonBroadcast : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_REQUEST_SENSOR_RELOAD == action) {
                val widgetId =
                    intent.getIntExtra(EXTRA_WIDGET_ID, SensorWidgetService.UNKNOWN_ELEMENT_ID)
                if (widgetId != SensorWidgetService.UNKNOWN_ELEMENT_ID) {
                    SensorWidgetService.startActionRequestReloadDataWidget(context, widgetId)
                }
            } else if (ACTION_REQUEST_SELECT_SENSOR == action) {
                val widgetId =
                    intent.getIntExtra(EXTRA_WIDGET_ID, SensorWidgetService.UNKNOWN_ELEMENT_ID)
                if (widgetId != SensorWidgetService.UNKNOWN_ELEMENT_ID) {
                    val activityIntent = Intent(context, SelectSensorForWidgetActivity::class.java)
                    activityIntent.putExtra(SelectSensorForWidgetActivity.WIDGET_ID, widgetId)
                    context.startActivity(activityIntent)
                }
            }
        }
    }

    companion object {

        private val ACTION_REQUEST_SENSOR_RELOAD =
            "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_REQUEST_SENSOR_RELOAD"
        private val ACTION_REQUEST_SELECT_SENSOR =
            "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_REQUEST_SELECT_SENSOR"

        private val EXTRA_WIDGET_ID =
            "com.arejas.dashboardofthings.presentation.ui.widget.WIDGET_ID"

        fun createActionSelectSensorWidget(context: Context, widgetId: Int): Intent {
            val intent = Intent(context, SensorWidgetButtonBroadcast::class.java)
            intent.action = ACTION_REQUEST_SELECT_SENSOR
            intent.putExtra(EXTRA_WIDGET_ID, widgetId)
            return intent
        }

        fun createActionRequestReloadDataWidget(context: Context, widgetId: Int): Intent {
            val intent = Intent(context, SensorWidgetButtonBroadcast::class.java)
            intent.action = ACTION_REQUEST_SENSOR_RELOAD
            intent.putExtra(EXTRA_WIDGET_ID, widgetId)
            return intent
        }
    }
}
