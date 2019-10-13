package com.arejas.dashboardofthings.domain.entities.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.arejas.dashboardofthings.utils.Enumerators;

import static androidx.room.ForeignKey.CASCADE;

import java.util.Map;

@Entity(tableName = "sensors",
        foreignKeys = @ForeignKey(entity = Network.class,
                parentColumns = "id",
                childColumns = "networkId",
                onDelete = CASCADE),
        indices = @Index(value = "networkId"))
public class Sensor implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    private String name;

    private String type;

    private String imageUri;

    @ColumnInfo(name = "networkId")
    private Integer networkId;

    private String httpRelativeUrl;

    private Map<String, String> httpHeaders;

    private Integer httpSecondsBetweenRequests;

    private String mqttTopicToSubscribe;

    private Enumerators.MqttQosLevel mqttQosLevel;

    private Enumerators.MessageType messageType;

    private Enumerators.DataType dataType;

    private String xmlOrJsonNode;

    private String rawRegularExpression;

    private String dataUnit;

    private String thresholdAboveCritical;

    private String thresholdAboveWarning;

    private String thresholdBelowCritical;

    private String thresholdBelowWarning;

    private String thresholdEqualsWarning;

    private String thresholdEqualsCritical;

    private Long locationLat;

    private Long localtionLong;

    private Boolean showInMainDashboard;

    public Sensor() {}

    protected Sensor(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        name = in.readString();
        type = in.readString();
        imageUri = in.readString();
        if (in.readByte() == 0) {
            networkId = null;
        } else {
            networkId = in.readInt();
        }
        httpRelativeUrl = in.readString();
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            String key = in.readString();
            String value = in.readString();
            httpHeaders.put(key,value);
        }
        if (in.readByte() == 0) {
            httpSecondsBetweenRequests = null;
        } else {
            httpSecondsBetweenRequests = in.readInt();
        }
        mqttTopicToSubscribe = in.readString();
        mqttQosLevel = Enumerators.MqttQosLevel.valueOf(in.readInt());
        messageType = Enumerators.MessageType.valueOf(in.readInt());
        dataType = Enumerators.DataType.valueOf(in.readInt());
        xmlOrJsonNode = in.readString();
        rawRegularExpression = in.readString();
        dataUnit = in.readString();
        thresholdAboveCritical = in.readString();
        thresholdAboveWarning = in.readString();
        thresholdBelowCritical = in.readString();
        thresholdBelowWarning = in.readString();
        thresholdEqualsWarning = in.readString();
        thresholdEqualsCritical = in.readString();
        if (in.readByte() == 0) {
            locationLat = null;
        } else {
            locationLat = in.readLong();
        }
        if (in.readByte() == 0) {
            localtionLong = null;
        } else {
            localtionLong = in.readLong();
        }
        byte tmpBShowInMainDashboard = in.readByte();
        showInMainDashboard = tmpBShowInMainDashboard == 0 ? null : tmpBShowInMainDashboard == 1;
    }

    public static final Creator<Sensor> CREATOR = new Creator<Sensor>() {
        @Override
        public Sensor createFromParcel(Parcel in) {
            return new Sensor(in);
        }

        @Override
        public Sensor[] newArray(int size) {
            return new Sensor[size];
        }
    };

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

    public Map<String, String> getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(Map<String, String> mHttpHeaders) {
        this.httpHeaders = mHttpHeaders;
    }

    public Integer getHttpSecondsBetweenRequests() {
        return httpSecondsBetweenRequests;
    }

    public void setHttpSecondsBetweenRequests(Integer iHttpSecondsBetweenRequests) {
        this.httpSecondsBetweenRequests = iHttpSecondsBetweenRequests;
    }

    public String getMqttTopicToSubscribe() {
        return mqttTopicToSubscribe;
    }

    public void setMqttTopicToSubscribe(String sMqttTopicToSubscribe) {
        this.mqttTopicToSubscribe = sMqttTopicToSubscribe;
    }

    public Enumerators.MqttQosLevel getMqttQosLevel() {
        return mqttQosLevel;
    }

    public void setMqttQosLevel(Enumerators.MqttQosLevel eMqttQosLevel) {
        this.mqttQosLevel = eMqttQosLevel;
    }

    public Enumerators.MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(Enumerators.MessageType eMessageType) {
        this.messageType = eMessageType;
    }

    public Enumerators.DataType getDataType() {
        return dataType;
    }

    public void setDataType(Enumerators.DataType eDataType) {
        this.dataType = eDataType;
    }

    public String getXmlOrJsonNode() {
        return xmlOrJsonNode;
    }

    public void setXmlOrJsonNode(String sXmlOrJsonNode) {
        this.xmlOrJsonNode = sXmlOrJsonNode;
    }

    public String getRawRegularExpression() {
        return rawRegularExpression;
    }

    public void setRawRegularExpression(String sRawAfterLimit) {
        this.rawRegularExpression = sRawAfterLimit;
    }

    public String getDataUnit() {
        return dataUnit;
    }

    public void setDataUnit(String sDataUnit) {
        this.dataUnit = sDataUnit;
    }

    public String getThresholdAboveCritical() {
        return thresholdAboveCritical;
    }

    public void setThresholdAboveCritical(String sThresholdAboveCritical) {
        this.thresholdAboveCritical = sThresholdAboveCritical;
    }

    public String getThresholdAboveWarning() {
        return thresholdAboveWarning;
    }

    public void setThresholdAboveWarning(String sThresholdAboveWarning) {
        this.thresholdAboveWarning = sThresholdAboveWarning;
    }

    public String getThresholdBelowCritical() {
        return thresholdBelowCritical;
    }

    public void setThresholdBelowCritical(String sThresholdBelowCritical) {
        this.thresholdBelowCritical = sThresholdBelowCritical;
    }

    public String getThresholdBelowWarning() {
        return thresholdBelowWarning;
    }

    public void setThresholdBelowWarning(String sThresholdBelowWarning) {
        this.thresholdBelowWarning = sThresholdBelowWarning;
    }

    public String getThresholdEqualsWarning() {
        return thresholdEqualsWarning;
    }

    public void setThresholdEqualsWarning(String sThresholdEqualsWarning) {
        this.thresholdEqualsWarning = sThresholdEqualsWarning;
    }

    public String getThresholdEqualsCritical() {
        return thresholdEqualsCritical;
    }

    public void setThresholdEqualsCritical(String sThresholdEqualsCritical) {
        this.thresholdEqualsCritical = sThresholdEqualsCritical;
    }

    public Long getLocationLat() {
        return locationLat;
    }

    public void setLocationLat(Long lLocationLat) {
        this.locationLat = lLocationLat;
    }

    public Long getLocaltionLong() {
        return localtionLong;
    }

    public void setLocaltionLong(Long lLocaltionLong) {
        this.localtionLong = lLocaltionLong;
    }

    public Boolean isShowInMainDashboard() {
        return showInMainDashboard;
    }

    public void setShowInMainDashboard(Boolean bShowInMainDashboard) {
        this.showInMainDashboard = bShowInMainDashboard;
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
        parcel.writeString(name);
        parcel.writeString(type);
        parcel.writeString(imageUri);
        if (networkId == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(networkId);
        }
        parcel.writeString(httpRelativeUrl);
        parcel.writeInt(httpHeaders.size());
        for(Map.Entry<String,String> entry : httpHeaders.entrySet()){
            parcel.writeString(entry.getKey());
            parcel.writeString(entry.getValue());
        }
        if (httpSecondsBetweenRequests == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(httpSecondsBetweenRequests);
        }
        parcel.writeString(mqttTopicToSubscribe);
        parcel.writeInt(mqttQosLevel.ordinal());
        parcel.writeInt(messageType.ordinal());
        parcel.writeInt(dataType.ordinal());
        parcel.writeString(xmlOrJsonNode);
        parcel.writeString(rawRegularExpression);
        parcel.writeString(dataUnit);
        parcel.writeString(thresholdAboveCritical);
        parcel.writeString(thresholdAboveWarning);
        parcel.writeString(thresholdBelowCritical);
        parcel.writeString(thresholdBelowWarning);
        parcel.writeString(thresholdEqualsWarning);
        parcel.writeString(thresholdEqualsCritical);
        if (locationLat == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(locationLat);
        }
        if (localtionLong == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(localtionLong);
        }
        parcel.writeByte((byte) (showInMainDashboard == null ? 0 : showInMainDashboard ? 1 : 2));
    }
}