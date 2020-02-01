package com.arejas.dashboardofthings.domain.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.widget.Toast

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.presentation.ui.activities.MainDashboardActivity
import com.arejas.dashboardofthings.presentation.ui.notifications.NotificationsHelper
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper
import com.arejas.dashboardofthings.utils.Utils

class BootDeviceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (Intent.ACTION_BOOT_COMPLETED == action) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(DotApplication.context)
            if (preferences.contains(DotApplication.context.getString(R.string.launch_on_startup_key)) && preferences.getBoolean(
                    DotApplication.context.getString(R.string.launch_on_startup_key),
                    false
                )
            ) {
                Utils.startControlService(context)
            }
        }
    }

}
