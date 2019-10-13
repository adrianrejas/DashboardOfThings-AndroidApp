package com.arejas.dashboardofthings.presentation.ui.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.presentation.ui.activities.MainDashboardActivity;
import com.arejas.dashboardofthings.utils.Enumerators;
import com.arejas.dashboardofthings.utils.rx.RxHelper;

import java.util.HashMap;
import java.util.Map;

public class NotificationsHelper {

    private static final String SENSOR_STATE_CHANNEL_ID = "sensor_State_notification_channel";

    public static final int FOREGROUND_SERVICE_NOTIFICATION_ID = Integer.MAX_VALUE;

    private static NotificationChannel sensorStateNotificationChannel = null;

    private static Map<Integer, Enumerators.NotificationType> sensorStateNotifications = new HashMap<>();

    public static void processStateNotificationForSensor(Context context, Sensor sensor, Enumerators.NotificationType state) {
        Enumerators.NotificationType currentState = sensorStateNotifications.get(sensor.getId());
        if (currentState == null) currentState = Enumerators.NotificationType.NONE;
        if (!state.equals(currentState)) {
            switch (state) {
                case NONE:
                    RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                            sensor.getName(), Enumerators.LogLevel.CRITICAL,
                            context.getString(R.string.log_notification_sensor_normal));
                    cancelNotification(context, sensor.getId());
                    break;
                case WARN:
                    RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                            sensor.getName(), Enumerators.LogLevel.WARN,
                            context.getString(R.string.log_notification_sensor_warning));
                    cancelNotification(context, sensor.getId());
                    showNotificationStateSensor(context, sensor.getId(), sensor.getName(),
                            context.getString(R.string.log_notification_sensor_warning));
                    break;
                case CRITICAL:
                    RxHelper.publishLog(sensor.getId(), Enumerators.ElementType.SENSOR,
                            sensor.getName(), Enumerators.LogLevel.CRITICAL,
                            context.getString(R.string.log_notification_sensor_critical));
                    cancelNotification(context, sensor.getId());
                    showNotificationStateSensor(context, sensor.getId(), sensor.getName(),
                            context.getString(R.string.log_notification_sensor_critical));
                    break;
            }
        }
    }

    public static Notification showNotificationForegroundService (Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (sensorStateNotificationChannel == null) {
                CharSequence name = context.getString(R.string.dashboard_of_things_channel_name);
                String description = context.getString(R.string.dashboard_of_things_channel_desc);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                sensorStateNotificationChannel = new NotificationChannel(SENSOR_STATE_CHANNEL_ID, name, importance);
                sensorStateNotificationChannel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(sensorStateNotificationChannel);
            }
        }
        Intent notificationIntent = new Intent(context, MainDashboardActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, SENSOR_STATE_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.control_service_notification_active))
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        return builder.build();
    }

    public static void showNotificationStateSensor (Context context, int notificationId, String notificationTitle,
                                        String notificationText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (sensorStateNotificationChannel == null) {
                CharSequence name = context.getString(R.string.dashboard_of_things_channel_name);
                String description = context.getString(R.string.dashboard_of_things_channel_desc);
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                sensorStateNotificationChannel = new NotificationChannel(SENSOR_STATE_CHANNEL_ID, name, importance);
                sensorStateNotificationChannel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(sensorStateNotificationChannel);
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, SENSOR_STATE_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    public static void cancelNotification(Context context, int notificationId) {
        NotificationManagerCompat.from(context).cancel(notificationId);
    }

}
