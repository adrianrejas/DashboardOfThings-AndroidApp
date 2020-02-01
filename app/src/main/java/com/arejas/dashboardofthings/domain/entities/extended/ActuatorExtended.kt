package com.arejas.dashboardofthings.domain.entities.extended

import android.os.Parcel

import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.utils.Enumerators

class ActuatorExtended : Actuator() {

    lateinit var networkName: String

    var networkType: Enumerators.NetworkType? = null

    var recentErrorLogs: Int? = null
}
