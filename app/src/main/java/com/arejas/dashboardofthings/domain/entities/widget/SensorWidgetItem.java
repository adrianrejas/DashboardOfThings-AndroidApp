package com.arejas.dashboardofthings.domain.entities.widget;

import android.os.Parcel;
import android.os.Parcelable;

import com.arejas.dashboardofthings.utils.Enumerators;

public class SensorWidgetItem implements Parcelable {

    private Integer sensorId;

    private String sensorName;

    private String sensorType;

    private Enumerators.DataType sensorDataType;

    private String sensorUnit;

    private String lastValueReceived;

    public SensorWidgetItem() {
    }

    protected SensorWidgetItem(Parcel in) {
        if (in.readByte() == 0) {
            sensorId = null;
        } else {
            sensorId = in.readInt();
        }
        sensorName = in.readString();
        sensorType = in.readString();
        sensorUnit = in.readString();
        sensorDataType = Enumerators.DataType.valueOf(in.readInt());
        lastValueReceived = in.readString();
    }

    public static final Creator<SensorWidgetItem> CREATOR = new Creator<SensorWidgetItem>() {
        @Override
        public SensorWidgetItem createFromParcel(Parcel in) {
            return new SensorWidgetItem(in);
        }

        @Override
        public SensorWidgetItem[] newArray(int size) {
            return new SensorWidgetItem[size];
        }
    };

    public Integer getSensorId() {
        return sensorId;
    }

    public void setSensorId(Integer sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public String getSensorUnit() {
        return sensorUnit;
    }

    public void setSensorUnit(String sensorUnit) {
        this.sensorUnit = sensorUnit;
    }

    public Enumerators.DataType getSensorDataType() {
        return sensorDataType;
    }

    public void setSensorDataType(Enumerators.DataType sensorDataType) {
        this.sensorDataType = sensorDataType;
    }

    public String getLastValueReceived() {
        return lastValueReceived;
    }

    public void setLastValueReceived(String lastValueReceived) {
        this.lastValueReceived = lastValueReceived;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (sensorId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(sensorId);
        }
        dest.writeString(sensorName);
        dest.writeString(sensorType);
        dest.writeString(sensorUnit);
        dest.writeInt(sensorDataType.ordinal());
        dest.writeString(lastValueReceived);
    }
}
