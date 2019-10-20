package com.arejas.dashboardofthings.domain.entities.extended;

import android.os.Parcel;

import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.Date;

public class SensorExtended extends Sensor {

    String networkName;

    private Enumerators.NetworkType networkType;

    private Integer recentErrorLogs;

    public SensorExtended() {}

    public SensorExtended(Parcel in) {
        super(in);
        networkName = in.readString();
        networkType = Enumerators.NetworkType.valueOf(in.readInt());
        if (in.readByte() == 0) {
            recentErrorLogs = null;
        } else {
            recentErrorLogs = in.readInt();
        }
    }

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

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(networkName);
        parcel.writeInt(networkType.ordinal());
        if (recentErrorLogs == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(recentErrorLogs);
        }
    }
}
