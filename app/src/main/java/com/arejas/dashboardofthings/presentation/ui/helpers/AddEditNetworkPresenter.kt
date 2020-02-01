package com.arejas.dashboardofthings.presentation.ui.helpers

import android.view.View
import android.widget.AdapterView
import android.widget.Spinner

interface AddEditNetworkPresenter {

    fun pickImage()

    fun cancelImagePicked()

    fun pickHttpCert()

    fun cancelHttpCert()

    fun pickMqttCert()

    fun cancelMqttCert()

    fun networkTypeSelected(ordinal: Int)

    fun httpAuthTypeSelected(ordinal: Int)

    fun httpUseSslChanged(checked: Boolean)

    fun mqttUseSslChanged(checked: Boolean)

}
