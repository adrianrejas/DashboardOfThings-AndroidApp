package com.arejas.dashboardofthings.domain.entities.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.Map;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "actuators",
        foreignKeys = @ForeignKey(entity = Network.class,
                parentColumns = "id",
                childColumns = "networkId",
                onDelete = CASCADE),
        indices = @Index(value = "networkId"))
public class Actuator {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    private String name;

    private String type;

    private String imageUri;

    @ColumnInfo(name = "networkId")
    private Integer networkId;

    private String httpRelativeUrl;

    private Enumerators.HttpMethod httpMethod;

    private Map<String, String> httpHeaders;

    private String httpMimeType;

    private String mqttTopicToPublish;

    private Enumerators.MqttQosLevel mqttQosLevel;

    private Enumerators.DataType dataType;

    private Float dataNumberMinimum;

    private Float dataNumberMaximum;

    private String dataFormatMessageToSend;

    private String dataUnit;

    private Double locationLat;

    private Double locationLong;

    private Boolean showInMainDashboard;

    public Actuator() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer iId) {
        this.id = iId;
    }

    public String getName() {
        return name;
    }

    public void setName(String sName) {
        this.name = sName;
    }

    public String getType() {
        return type;
    }

    public void setType(String sType) {
        this.type = sType;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String sImageUri) {
        this.imageUri = sImageUri;
    }

    public Integer getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Integer iNetworkId) {
        this.networkId = iNetworkId;
    }

    public String getHttpRelativeUrl() {
        return httpRelativeUrl;
    }

    public void setHttpRelativeUrl(String sHttpRelativeUrl) {
        this.httpRelativeUrl = sHttpRelativeUrl;
    }

    public Enumerators.HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(Enumerators.HttpMethod eHttpMethod) {
        this.httpMethod = eHttpMethod;
    }

    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(Map<String, String> mHttpHeaders) {
        this.httpHeaders = mHttpHeaders;
    }

    public String getMqttTopicToPublish() {
        return mqttTopicToPublish;
    }

    public void setMqttTopicToPublish(String sMqttTopicToPublish) {
        this.mqttTopicToPublish = sMqttTopicToPublish;
    }

    public Enumerators.MqttQosLevel getMqttQosLevel() {
        return mqttQosLevel;
    }

    public void setMqttQosLevel(Enumerators.MqttQosLevel eMqttQosLevel) {
        this.mqttQosLevel = eMqttQosLevel;
    }

    public Enumerators.DataType getDataType() {
        return dataType;
    }

    public void setDataType(Enumerators.DataType eDataType) {
        this.dataType = eDataType;
    }

    public Float getDataNumberMinimum() {
        return dataNumberMinimum;
    }

    public void setDataNumberMinimum(Float dataNumberMinimum) {
        this.dataNumberMinimum = dataNumberMinimum;
    }

    public Float getDataNumberMaximum() {
        return dataNumberMaximum;
    }

    public void setDataNumberMaximum(Float dataNumberMaximum) {
        this.dataNumberMaximum = dataNumberMaximum;
    }

    public String getDataFormatMessageToSend() {
        return dataFormatMessageToSend;
    }

    public void setDataFormatMessageToSend(String sDataFormatMessageToSend) {
        this.dataFormatMessageToSend = sDataFormatMessageToSend;
    }

    public String getHttpMimeType() {
        return httpMimeType;
    }

    public void setHttpMimeType(String httpMimeType) {
        this.httpMimeType = httpMimeType;
    }

    public String getDataUnit() {
        return dataUnit;
    }

    public void setDataUnit(String sDataUnit) {
        this.dataUnit = sDataUnit;
    }

    public Double getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(Double lLocationLat) {
        this.locationLat = lLocationLat;
    }

    public Double getLocationLong() {
        return locationLong;
    }

    public void setLocationLong(Double lLocaltionLong) {
        this.locationLong = lLocaltionLong;
    }

    public Boolean isShowInMainDashboard() {
        return showInMainDashboard;
    }

    public void setShowInMainDashboard(Boolean bShowInMainDashboard) {
        this.showInMainDashboard = bShowInMainDashboard;
    }

}
