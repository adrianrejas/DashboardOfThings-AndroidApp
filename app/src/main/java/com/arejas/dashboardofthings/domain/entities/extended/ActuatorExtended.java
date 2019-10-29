package com.arejas.dashboardofthings.domain.entities.extended;

import android.os.Parcel;

import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.utils.Enumerators;

public class ActuatorExtended extends Actuator {

    String networkName;

    private Enumerators.NetworkType networkType;

    private Integer recentErrorLogs;

    public ActuatorExtended() {}

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public Enumerators.NetworkType getNetworkType() {
        return networkType;
    }

    public void setNetworkType(Enumerators.NetworkType networkType) {
        this.networkType = networkType;
    }

    public Integer getRecentErrorLogs() {
        return recentErrorLogs;
    }

    public void setRecentErrorLogs(Integer recentErrorLogs) {
        this.recentErrorLogs = recentErrorLogs;
    }
}
