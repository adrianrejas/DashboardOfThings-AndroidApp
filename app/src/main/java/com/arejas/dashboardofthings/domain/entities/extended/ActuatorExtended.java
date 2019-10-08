package com.arejas.dashboardofthings.domain.entities.extended;

import android.os.Parcel;

import com.arejas.dashboardofthings.domain.entities.Actuator;
import com.arejas.dashboardofthings.domain.entities.Sensor;
import com.arejas.dashboardofthings.utils.Enumerators;

public class ActuatorExtended extends Actuator {

    String networkName;

    private Enumerators.NetworkType networkType;

    public ActuatorExtended() {}

    public ActuatorExtended(Parcel in) {
        super(in);
        networkName = in.readString();
        networkType = Enumerators.NetworkType.valueOf(in.readInt());
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

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(networkName);
        parcel.writeInt(networkType.ordinal());
    }
}
