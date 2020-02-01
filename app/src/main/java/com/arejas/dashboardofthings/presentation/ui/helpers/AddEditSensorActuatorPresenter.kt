package com.arejas.dashboardofthings.presentation.ui.helpers

interface AddEditSensorActuatorPresenter {

    fun pickImage()

    fun cancelImagePicked()

    fun pickLocation()

    fun cancelLocationPicked()

    fun addHttpHeader()

    fun cancelHttpHeader(headerName: String)

    fun networkSelected(ordinal: Int)

    fun messageTypeSelected(ordinal: Int)

    fun dataTypeSelected(ordinal: Int)

}
