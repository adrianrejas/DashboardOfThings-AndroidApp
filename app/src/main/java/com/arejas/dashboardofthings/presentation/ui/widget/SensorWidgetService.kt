package com.arejas.dashboardofthings.presentation.ui.widget

import android.app.IntentService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.core.app.JobIntentService
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.domain.entities.widget.SensorWidgetItem
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase
import com.arejas.dashboardofthings.utils.Enumerators
import com.arejas.dashboardofthings.utils.Utils

import java.util.HashMap

import javax.inject.Inject

import dagger.android.AndroidInjection

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 *
 *
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
class SensorWidgetService : JobIntentService() {

    @Inject
    var sensorManagementUseCase: SensorManagementUseCase? = null

    override fun onCreate() {
        super.onCreate()

        // Dependency injection
        AndroidInjection.inject(this)
    }

    override fun onHandleWork(intent: Intent) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_UPDATE_WIDGETS == action) {
                handleActionUpdateWidgets()
            } else if (ACTION_UPDATE_WIDGET == action) {
                val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0)
                val sensorId = intent.getIntExtra(EXTRA_SENSOR_ID, UNKNOWN_ELEMENT_ID)
                handleActionSetSensorForWidget(widgetId, sensorId)
            } else if (ACTION_UPDATE_WIDGET_SET_DATA == action) {
                val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0)
                val value = intent.getStringExtra(EXTRA_VALUE)
                handleActionUpdateWidgetSetData(widgetId, value)
            } else if (ACTION_SET_SENSOR_FOR_WIDGET == action) {
                val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0)
                val sensorId = intent.getIntExtra(EXTRA_SENSOR_ID, UNKNOWN_ELEMENT_ID)
                handleActionSetSensorForWidget(widgetId, sensorId)
            } else if (ACTION_REQUEST_RELOAD_DATA_WIDGET == action) {
                val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0)
                handleActionRequestReloadData(widgetId)
            } else if (ACTION_REMOVE_WIDGET == action) {
                val widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, 0)
                handleActionRemoveWidget(widgetId)
            }
        }
    }

    /**
     * Handle action UpdateWidgets
     */
    private fun handleActionUpdateWidgets() {
        // Get app widget manager and the IDs of the widget instances deployed
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val appWidgetIds =
            appWidgetManager.getAppWidgetIds(ComponentName(this, SensorWidget::class.java))
        // For each of the widget instances
        val sensorIdToWidgetIdMap = HashMap<Int, Int>()
        for (widgetId in appWidgetIds) {
            // Get sensor Id for widget from preferences
            val sensorId = getSensorIdForWidgetId(this, widgetId)
            // if sensor id selected, put in a map for processing it
            // if not, launch the update of the widget for the selection of sensor
            if (sensorId != UNKNOWN_ELEMENT_ID) {
                sensorIdToWidgetIdMap[sensorId] = widgetId
            } else {
                SensorWidget.updateAppWidgetSelectSensor(
                    applicationContext,
                    appWidgetManager,
                    widgetId
                )
            }
        }
        val keys = sensorIdToWidgetIdMap.keys
        val sensorIds = IntArray(keys.size)
        var i = 0
        for (sensorId in keys) {
            sensorIds[i] = sensorId
            i++
        }
        sensorManagementUseCase!!.getSensorInfoForWidgets(sensorIds) { dataList ->
            //Now update all the widgets
            if (dataList != null) {
                for (sensorInfo in dataList) {
                    val widgetId = sensorIdToWidgetIdMap[sensorInfo.sensorId]!!
                    saveUnitUsedForWidgetId(applicationContext, widgetId, sensorInfo.sensorUnit)
                    SensorWidget.updateAppWidget(
                        applicationContext,
                        appWidgetManager,
                        widgetId,
                        sensorInfo
                    )
                }
            }
        }
    }

    /**
     * Handle action UpdateWidgets
     */
    private fun handleActionUpdateWidget(widgetId: Int) {
        // Get app widget manager and the IDs of the widget instances deployed
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val sensorId = getSensorIdForWidgetId(this, widgetId)
        if (sensorId != UNKNOWN_ELEMENT_ID) {
            sensorManagementUseCase!!.getSensorInfoForWidget(sensorId) { sensorInfo ->
                //Now update the widget
                if (sensorInfo != null) {
                    SensorWidget.updateAppWidget(
                        applicationContext,
                        appWidgetManager,
                        widgetId,
                        sensorInfo
                    )
                    saveUnitUsedForWidgetId(applicationContext, widgetId, sensorInfo.sensorUnit)
                    saveDataTypeUsedForWidgetId(
                        applicationContext,
                        widgetId,
                        sensorInfo.sensorDataType!!.ordinal
                    )
                }
            }
        }
    }

    /**
     * Handle action SetSensorForWidget in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionSetSensorForWidget(widgetId: Int, sensorId: Int) {
        // get app widget manager
        val appWidgetManager = AppWidgetManager.getInstance(this)
        // Save preferences for widget Id
        saveSensorIdForWidgetId(applicationContext, widgetId, sensorId)
        //Set now widget as loading
        SensorWidget.updateAppWidgetAsLoading(this, appWidgetManager, widgetId)
        // Request data of the sensor and update the widget with it
        sensorManagementUseCase!!.getSensorInfoForWidget(sensorId) { sensorInfo ->
            //Now update the widget
            if (sensorInfo != null) {
                SensorWidget.updateAppWidget(
                    applicationContext,
                    appWidgetManager,
                    widgetId,
                    sensorInfo
                )
                saveUnitUsedForWidgetId(applicationContext, widgetId, sensorInfo.sensorUnit)
                saveDataTypeUsedForWidgetId(
                    applicationContext,
                    widgetId,
                    sensorInfo.sensorDataType!!.ordinal
                )
            }
        }
    }

    /**
     * Handle action UpdateWidgetSetData in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionUpdateWidgetSetData(widgetId: Int, value: String?) {
        // get app widget manager
        val appWidgetManager = AppWidgetManager.getInstance(this)
        if (value != null) {
            val unit = getUnitUsedForWidgetId(applicationContext, widgetId)
            var dataType: Enumerators.DataType? = null
            val dataTypeInt = getDataTypeUsedForWidgetId(applicationContext, widgetId)
            if (dataTypeInt != UNKNOWN_ELEMENT_ID) {
                dataType = Enumerators.DataType.valueOf(dataTypeInt)
            }
            SensorWidget.updateAppWidgetSetData(
                applicationContext,
                appWidgetManager,
                widgetId,
                value,
                dataType,
                unit
            )
        }
    }

    /**
     * Handle action RequestReloadData in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionRequestReloadData(widgetId: Int) {
        // get app widget manager
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val sensorId = getSensorIdForWidgetId(applicationContext, widgetId)
        sensorManagementUseCase!!.requestSensorReload(sensorId)
        if (sensorId != UNKNOWN_ELEMENT_ID) {
            sensorManagementUseCase!!.requestSensorReload(sensorId)
        }
    }

    /**
     * Handle action RemoveWidget in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionRemoveWidget(widgetId: Int) {
        // Remove preferences for widget Id
        removeSensorIdForWidgetId(applicationContext, widgetId)
        removeUnitUsedForWidgetId(applicationContext, widgetId)
        removeDataTypeUsedForWidgetId(applicationContext, widgetId)
    }

    companion object {

        val UNKNOWN_ELEMENT_ID = -1
        private val WIDGET_PREFERENCES = "widgetPreferences"

        private val JOB_ID_UPDATE = 1
        private val JOB_ID_SET_SENSOR = 1
        private val JOB_ID_REMOVE_WIDGET = 1

        private val ACTION_UPDATE_WIDGETS =
            "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_UPDATE_WIDGETS"
        private val ACTION_UPDATE_WIDGET =
            "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_UPDATE_WIDGET"
        private val ACTION_UPDATE_WIDGET_SET_DATA =
            "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_UPDATE_WIDGET_SET_DATA"
        private val ACTION_SET_SENSOR_FOR_WIDGET =
            "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_SET_SENSOR_FOR_WIDGET"
        private val ACTION_REQUEST_RELOAD_DATA_WIDGET =
            "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_REQUEST_RELOAD_DATA_WIDGET"
        private val ACTION_REMOVE_WIDGET =
            "com.arejas.dashboardofthings.presentation.ui.widget.ACTION_REMOVE_WIDGET"

        private val EXTRA_WIDGET_ID = "WIDGET_ID"
        private val EXTRA_SENSOR_ID = "SENSOR_ID"
        private val EXTRA_VALUE = "VALUE"

        /**
         * Starts this service to perform action UpdateWidgets with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionUpdateWidgets(context: Context) {
            val intent = Intent(context, SensorWidgetService::class.java)
            intent.action = ACTION_UPDATE_WIDGETS
            JobIntentService.enqueueWork(
                context,
                SensorWidgetService::class.java,
                JOB_ID_UPDATE,
                intent
            )
        }

        /**
         * Starts this service to perform action UpdateWidget with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionUpdateWidget(context: Context, widgetId: Int) {
            val intent = Intent(context, SensorWidgetService::class.java)
            intent.action = ACTION_UPDATE_WIDGET
            intent.putExtra(EXTRA_WIDGET_ID, widgetId)
            JobIntentService.enqueueWork(
                context,
                SensorWidgetService::class.java,
                JOB_ID_UPDATE,
                intent
            )
        }

        /**
         * Starts this service to perform action UpdateWidgetSetData with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionUpdateWidgetSetData(context: Context, widgetId: Int, value: String) {
            val intent = Intent(context, SensorWidgetService::class.java)
            intent.action = ACTION_UPDATE_WIDGET_SET_DATA
            intent.putExtra(EXTRA_WIDGET_ID, widgetId)
            intent.putExtra(EXTRA_VALUE, value)
            JobIntentService.enqueueWork(
                context,
                SensorWidgetService::class.java,
                JOB_ID_UPDATE,
                intent
            )
        }

        /**
         * Starts this service to perform action SetSensorForWidget with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionSetSensorForWidget(context: Context, widgetId: Int, sensorId: Int) {
            val intent = Intent(context, SensorWidgetService::class.java)
            intent.action = ACTION_SET_SENSOR_FOR_WIDGET
            intent.putExtra(EXTRA_WIDGET_ID, widgetId)
            intent.putExtra(EXTRA_SENSOR_ID, sensorId)
            JobIntentService.enqueueWork(
                context,
                SensorWidgetService::class.java,
                JOB_ID_SET_SENSOR,
                intent
            )
        }

        /**
         * Starts this service to perform action RequestReloadData with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionRequestReloadDataWidget(context: Context, widgetId: Int) {
            val intent = Intent(context, SensorWidgetService::class.java)
            intent.action = ACTION_REQUEST_RELOAD_DATA_WIDGET
            intent.putExtra(EXTRA_WIDGET_ID, widgetId)
            JobIntentService.enqueueWork(
                context,
                SensorWidgetService::class.java,
                JOB_ID_SET_SENSOR,
                intent
            )
        }

        /**
         * Starts this service to perform action RemoveWidget with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        fun startActionRemoveWidget(context: Context, widgetId: Int) {
            val intent = Intent(context, SensorWidgetService::class.java)
            intent.action = ACTION_REMOVE_WIDGET
            intent.putExtra(EXTRA_WIDGET_ID, widgetId)
            JobIntentService.enqueueWork(
                context,
                SensorWidgetService::class.java,
                JOB_ID_REMOVE_WIDGET,
                intent
            )
        }

        fun getWidgetIdForSensorId(context: Context, sensorId: Int): Int {
            return context.getSharedPreferences(WIDGET_PREFERENCES, 0)
                .getInt(
                    context.getString(R.string.preference_widget_id_for_sensor, sensorId),
                    UNKNOWN_ELEMENT_ID
                )
        }

        private fun getSensorIdForWidgetId(context: Context, widgetId: Int): Int {
            return context.getSharedPreferences(WIDGET_PREFERENCES, 0)
                .getInt(
                    context.getString(R.string.preference_sensor_id_for_widget, widgetId),
                    UNKNOWN_ELEMENT_ID
                )
        }

        private fun saveSensorIdForWidgetId(context: Context, widgetId: Int, sensorId: Int) {
            val editor = context.getSharedPreferences(WIDGET_PREFERENCES, 0).edit()
            editor.putInt(
                context.getString(R.string.preference_sensor_id_for_widget, widgetId),
                sensorId
            )
            editor.putInt(
                context.getString(R.string.preference_widget_id_for_sensor, sensorId),
                widgetId
            )
            editor.apply()
            editor.clear()
        }

        private fun removeSensorIdForWidgetId(context: Context, widgetId: Int) {
            val prefs = context.getSharedPreferences(WIDGET_PREFERENCES, 0)
            val sensorId = prefs.getInt(
                context.getString(R.string.preference_sensor_id_for_widget, widgetId),
                UNKNOWN_ELEMENT_ID
            )
            val editor = prefs.edit()
            editor.remove(context.getString(R.string.preference_sensor_id_for_widget, widgetId))
            if (sensorId != UNKNOWN_ELEMENT_ID)
                editor.remove(context.getString(R.string.preference_widget_id_for_sensor, sensorId))
            editor.apply()
            editor.clear()
        }

        private fun getUnitUsedForWidgetId(context: Context, widgetId: Int): String? {
            return context.getSharedPreferences(WIDGET_PREFERENCES, 0)
                .getString(
                    context.getString(R.string.preference_unit_used_for_widget, widgetId),
                    null
                )
        }

        private fun saveUnitUsedForWidgetId(context: Context, widgetId: Int, unitUsed: String?) {
            if (unitUsed != null) {
                val editor = context.getSharedPreferences(WIDGET_PREFERENCES, 0).edit()
                editor.putString(
                    context.getString(
                        R.string.preference_unit_used_for_widget,
                        widgetId
                    ), unitUsed
                )
                editor.apply()
                editor.clear()
            }
        }

        private fun removeUnitUsedForWidgetId(context: Context, widgetId: Int) {
            val prefs = context.getSharedPreferences(WIDGET_PREFERENCES, 0)
            val unitUsed = prefs.getString(
                context.getString(
                    R.string.preference_unit_used_for_widget,
                    widgetId
                ), null
            )
            if (unitUsed != null) {
                val editor = prefs.edit()
                editor.remove(context.getString(R.string.preference_unit_used_for_widget, widgetId))
                editor.apply()
                editor.clear()
            }
        }

        private fun getDataTypeUsedForWidgetId(context: Context, widgetId: Int): Int {
            return context.getSharedPreferences(WIDGET_PREFERENCES, 0)
                .getInt(
                    context.getString(R.string.preference_data_type_used_for_widget, widgetId),
                    UNKNOWN_ELEMENT_ID
                )
        }

        private fun saveDataTypeUsedForWidgetId(
            context: Context,
            widgetId: Int,
            dataTypeUsed: Int
        ) {
            if (dataTypeUsed != UNKNOWN_ELEMENT_ID) {
                val editor = context.getSharedPreferences(WIDGET_PREFERENCES, 0).edit()
                editor.putInt(
                    context.getString(
                        R.string.preference_data_type_used_for_widget,
                        widgetId
                    ), dataTypeUsed
                )
                editor.apply()
                editor.clear()
            }
        }

        private fun removeDataTypeUsedForWidgetId(context: Context, widgetId: Int) {
            val prefs = context.getSharedPreferences(WIDGET_PREFERENCES, 0)
            val dataTypeUsed = prefs.getInt(
                context.getString(R.string.preference_data_type_used_for_widget, widgetId),
                UNKNOWN_ELEMENT_ID
            )
            if (dataTypeUsed != UNKNOWN_ELEMENT_ID) {
                val editor = prefs.edit()
                editor.remove(
                    context.getString(
                        R.string.preference_data_type_used_for_widget,
                        widgetId
                    )
                )
                editor.apply()
                editor.clear()
            }
        }
    }

}
