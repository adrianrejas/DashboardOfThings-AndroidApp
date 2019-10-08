package com.arejas.dashboardofthings.domain.entities.extended;

import android.os.Parcel;

import com.arejas.dashboardofthings.domain.entities.Sensor;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.Date;

public class SensorExtended extends Sensor {

    String networkName;

    private Enumerators.NetworkType networkType;

    private String lastValue;

    private Date dateReceivedLastValue;


    public SensorExtended() {}

    public SensorExtended(Parcel in) {
        super(in);
        networkName = in.readString();
        networkType = Enumerators.NetworkType.valueOf(in.readInt());
        lastValue = in.readString();
        dateReceivedLastValue = new Date(in.readLong());
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

    public String getLastValue() {
        return lastValue;
    }

    public void setLastValue(String lastValue) {
        this.lastValue = lastValue;
    }

    public Date getDateReceivedLastValue() {
        return dateReceivedLastValue;
    }

    public void setDateReceivedLastValue(Date dateReceivedLastValue) {
        this.dateReceivedLastValue = dateReceivedLastValue;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel, i);
        parcel.writeString(networkName);
        parcel.writeInt(networkType.ordinal());
        parcel.writeString(lastValue);
        parcel.writeLong(dateReceivedLastValue.getTime());
    }
}
