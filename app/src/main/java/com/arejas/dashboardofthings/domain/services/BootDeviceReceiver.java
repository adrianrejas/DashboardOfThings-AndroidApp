package com.arejas.dashboardofthings.domain.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.presentation.ui.activities.MainDashboardActivity;
import com.arejas.dashboardofthings.presentation.ui.notifications.NotificationsHelper;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;
import com.arejas.dashboardofthings.utils.Utils;

public class BootDeviceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(DotApplication.getContext());
            if ((preferences.contains(DotApplication.getContext().getString(R.string.launch_on_startup_key))) &&
                    (preferences.getBoolean(DotApplication.getContext().getString(R.string.launch_on_startup_key), false))) {
                Utils.startControlService(context);
            }
        }
    }

}
