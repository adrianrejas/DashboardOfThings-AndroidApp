package com.arejas.dashboardofthings.presentation.ui.helpers

interface ActuatorDetailsListener {

    fun sendInteger()

    fun sendFloat()

    fun sendBooleanFalse()

    fun sendBooleanTrue()

    fun sendString()

}
