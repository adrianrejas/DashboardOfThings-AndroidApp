package com.arejas.dashboardofthings.presentation.ui.notifications

import android.widget.Toast

import com.arejas.dashboardofthings.DotApplication

object ToastHelper {

    private var mToast: Toast? = null

    fun showToast(message: String) {
        if (mToast != null) mToast!!.cancel()
        mToast = Toast.makeText(DotApplication.context, message, Toast.LENGTH_LONG)
        mToast!!.show()
    }

}
