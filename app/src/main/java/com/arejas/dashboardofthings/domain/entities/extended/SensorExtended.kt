package com.arejas.dashboardofthings.domain.entities.extended

import android.os.Parcel

import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.utils.Enumerators

import java.util.Date

class SensorExtended : Sensor() {

    lateinit var networkName: String

    var networkType: Enumerators.NetworkType? = null

    var recentErrorLogs: Int? = null
}