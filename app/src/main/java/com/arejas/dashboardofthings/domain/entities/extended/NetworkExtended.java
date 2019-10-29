package com.arejas.dashboardofthings.domain.entities.extended;

import android.os.Parcel;

import com.arejas.dashboardofthings.domain.entities.database.Network;

public class NetworkExtended extends Network {

    private Integer recentErrorLogs;

    public NetworkExtended() {}

    public Integer getRecentErrorLogs() {
        return recentErrorLogs;
    }

    public void setRecentErrorLogs(Integer recentErrorLogs) {
        this.recentErrorLogs = recentErrorLogs;
    }
}
