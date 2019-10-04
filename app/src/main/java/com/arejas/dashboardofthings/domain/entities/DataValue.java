package com.arejas.dashboardofthings.domain.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.arejas.dashboardofthings.data.sources.database.converters.DotTypeConverters;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.Date;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "values",
        foreignKeys = @ForeignKey(entity = Sensor.class,
                parentColumns = "id",
                childColumns = "sensorId",
                onDelete = CASCADE),
        indices = @Index(value = {"sensorId", "dateReceived"}))
public class DataValue implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    @ColumnInfo(name = "sensorId")
    private Integer sensorId;

    private String value;

    private Date dateReceived;

    public DataValue() {}

    protected DataValue(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        if (in.readByte() == 0) {
            sensorId = null;
        } else {
            sensorId = in.readInt();
        }
        value = in.readString();
        dateReceived = new Date(in.readLong());
    }

    public static final Creator<DataValue> CREATOR = new Creator<DataValue>() {
        @Override
        public DataValue createFromParcel(Parcel in) {
            return new DataValue(in);
        }

        @Override
        public DataValue[] newArray(int size) {
            return new DataValue[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSensorId() {
        return sensorId;
    }

    public void setSensorId(Integer iSensorId) {
        this.sensorId = iSensorId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String sValue) {
        this.value = sValue;
    }

    public Date getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(Date dateReceived) {
        this.dateReceived = dateReceived;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(id);
        }
        if (sensorId == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(sensorId);
        }
        parcel.writeString(value);
        parcel.writeLong(dateReceived.getTime());
    }
}
