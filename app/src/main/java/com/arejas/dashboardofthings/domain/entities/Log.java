package com.arejas.dashboardofthings.domain.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.arejas.dashboardofthings.data.sources.database.converters.DotTypeConverters;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.Date;

@Entity(tableName = "logs",
        indices = @Index(value = {"elementId", "dateRegistered"}))
public class Log implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    private Integer elementId;

    private Enumerators.ElementType elementType;

    private String logMessage;

    private Enumerators.LogLevel logLevel;

    private Date dateRegistered;

    public Log() {}

    protected Log(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        if (in.readByte() == 0) {
            elementId = null;
        } else {
            elementId = in.readInt();
        }
        elementType = Enumerators.ElementType.valueOf(in.readInt());
        logMessage = in.readString();
        logLevel = Enumerators.LogLevel.valueOf(in.readInt());
        dateRegistered = new Date(in.readLong());
    }

    public static final Creator<Log> CREATOR = new Creator<Log>() {
        @Override
        public Log createFromParcel(Parcel in) {
            return new Log(in);
        }

        @Override
        public Log[] newArray(int size) {
            return new Log[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getElementId() {
        return elementId;
    }

    public void setElementId(Integer iElementId) {
        this.elementId = iElementId;
    }

    public Enumerators.ElementType getElementType() {
        return elementType;
    }

    public void setElementType(Enumerators.ElementType eElementType) {
        this.elementType = eElementType;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String sLogMessage) {
        this.logMessage = sLogMessage;
    }

    public Enumerators.LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Enumerators.LogLevel sLogLevel) {
        this.logLevel = sLogLevel;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
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
        if (elementId == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(elementId);
        }
        parcel.writeInt(elementType.ordinal());
        parcel.writeString(logMessage);
        parcel.writeInt(logLevel.ordinal());
        parcel.writeLong(dateRegistered.getTime());
    }
}
