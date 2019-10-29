package com.arejas.dashboardofthings.domain.entities.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "values",
        foreignKeys = @ForeignKey(entity = Sensor.class,
                parentColumns = "id",
                childColumns = "sensorId",
                onDelete = CASCADE),
        indices = @Index(value = {"sensorId", "dateReceived"}))
public class DataValue {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    @ColumnInfo(name = "sensorId")
    private Integer sensorId;

    private String value;

    private Date dateReceived;

    public DataValue() {}

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

}
