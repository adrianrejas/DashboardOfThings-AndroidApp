package com.arejas.dashboardofthings.domain.entities.extended;

import android.os.Parcel;

import com.arejas.dashboardofthings.domain.entities.database.Network;

public class NetworkExtended extends Network {

    private Integer recentErrorLogs;

    public NetworkExtended() {}

    public NetworkExtended(Parcel in) {
        super(in);
        if (in.readByte() == 0) {
            recentErrorLogs = null;
        } else {
            recentErrorLogs = in.readInt();
        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        if (recentErrorLogs == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(recentErrorLogs);
        }
    }

    public Integer getRecentErrorLogs() {
        return recentErrorLogs;
    }

    public void setRecentErrorLogs(Integer recentErrorLogs) {
        this.recentErrorLogs = recentErrorLogs;
    }
}
