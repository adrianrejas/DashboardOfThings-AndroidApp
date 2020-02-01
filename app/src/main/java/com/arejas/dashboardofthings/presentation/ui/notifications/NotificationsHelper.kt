package com.arejas.dashboardofthings.presentation.ui.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.presentation.ui.activities.MainDashboardActivity
import com.arejas.dashboardofthings.utils.Enumerators
import com.arejas.dashboardofthings.utils.rx.RxHelper

import java.util.HashMap

object NotificationsHelper {

    private val SENSOR_STATE_CHANNEL_ID = "sensor_State_notification_channel"

    val FOREGROUND_SERVICE_NOTIFICATION_ID = Integer.MAX_VALUE

    private var sensorStateNotificationChannel: NotificationChannel? = null

    private val sensorStateNotifications = HashMap<Int, Enumerators.NotificationType>()

    fun processStateNotificationForSensor(
        context: Context,
        sensor: Sensor,
        state: Enumerators.NotificationType
    ) {
        val currentState = sensorStateNotifications[sensor.id]
        if (state != currentState) {
            when (state) {
                Enumerators.NotificationType.NONE -> {
                    RxHelper.publishLog(
                        sensor.id, Enumerators.ElementType.SENSOR,
                        sensor.name, Enumerators.LogLevel.NOTIF_NONE,
                        context.getString(R.string.log_notification_sensor_normal)
                    )
                    cancelNotification(context, sensor.id!!)
                }
                Enumerators.NotificationType.WARNING -> {
                    RxHelper.publishLog(
                        sensor.id, Enumerators.ElementType.SENSOR,
                        sensor.name, Enumerators.LogLevel.NOTIF_WARN,
                        context.getString(R.string.log_notification_sensor_warning)
                    )
                    cancelNotification(context, sensor.id!!)
                    showNotificationStateSensor(
                        context, sensor.id!!, sensor.name,
                        context.getString(R.string.log_notification_sensor_warning)
                    )
                }
                Enumerators.NotificationType.CRITICAL -> {
                    RxHelper.publishLog(
                        sensor.id, Enumerators.ElementType.SENSOR,
                        sensor.name, Enumerators.LogLevel.NOTIF_CRITICAL,
                        context.getString(R.string.log_notification_sensor_critical)
                    )
                    cancelNotification(context, sensor.id!!)
                    showNotificationStateSensor(
                        context, sensor.id!!, sensor.name,
                        context.getString(R.string.log_notification_sensor_critical)
                    )
                }
            }
            sensorStateNotifications[sensor.id!!] = currentState!!
        }
    }

    fun showNotificationForegroundService(context: Context): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (sensorStateNotificationChannel == null) {
                val name = context.getString(R.string.dashboard_of_things_channel_name)
                val description = context.getString(R.string.dashboard_of_things_channel_desc)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                sensorStateNotificationChannel =
                    NotificationChannel(SENSOR_STATE_CHANNEL_ID, name, importance)
                sensorStateNotificationChannel!!.description = description
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(sensorStateNotificationChannel!!)
            }
        }
        val notificationIntent = Intent(context, MainDashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
        val builder = NotificationCompat.Builder(context, SENSOR_STATE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.control_service_notification_active))
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        return builder.build()
    }

    fun showNotificationStateSensor(
        context: Context, notificationId: Int, notificationTitle: String?,
        notificationText: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (sensorStateNotificationChannel == null) {
                val name = context.getString(R.string.dashboard_of_things_channel_name)
                val description = context.getString(R.string.dashboard_of_things_channel_desc)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                sensorStateNotificationChannel =
                    NotificationChannel(SENSOR_STATE_CHANNEL_ID, name, importance)
                sensorStateNotificationChannel!!.description = description
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(sensorStateNotificationChannel!!)
            }
        }
        val builder = NotificationCompat.Builder(context, SENSOR_STATE_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

}
