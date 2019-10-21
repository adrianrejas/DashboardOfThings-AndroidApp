package com.arejas.dashboardofthings.presentation.ui.notifications;

import android.widget.Toast;

import com.arejas.dashboardofthings.DotApplication;

public class ToastHelper {

    private static Toast mToast;

    public static void showToast(String message) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(DotApplication.getContext(), message, Toast.LENGTH_SHORT);
        mToast.show();
    }

}
