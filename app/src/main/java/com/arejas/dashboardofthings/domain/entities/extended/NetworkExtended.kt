package com.arejas.dashboardofthings.domain.entities.extended

import android.os.Parcel

import com.arejas.dashboardofthings.domain.entities.database.Network

class NetworkExtended : Network() {

    var recentErrorLogs: Int? = null
}
