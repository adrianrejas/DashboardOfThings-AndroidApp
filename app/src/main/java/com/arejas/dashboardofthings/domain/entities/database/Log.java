package com.arejas.dashboardofthings.domain.entities.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.Date;

@Entity(tableName = "logs",
        indices = @Index(value = {"elementId", "dateRegistered"}))
public class Log {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    private Integer elementId;

    String elementName;

    private Enumerators.ElementType elementType;

    private String logMessage;

    private Enumerators.LogLevel logLevel;

    private Date dateRegistered;

    public Log() {}

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

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
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

}
