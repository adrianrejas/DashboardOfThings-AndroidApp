package com.arejas.dashboardofthings.domain.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.arejas.dashboardofthings.presentation.ui.notifications.NotificationsHelper;

public class BootDeviceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            ControlService.startAsForegroundService(context);
        }
    }

}