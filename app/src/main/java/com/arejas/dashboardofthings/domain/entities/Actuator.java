package com.arejas.dashboardofthings.domain.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
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
public class Actuator implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private Integer id;

    private String name;

    private String type;

    private String imageUri;

    @ColumnInfo(name = "networkId")
    private Integer networkId;

    @Ignore
    private Network network;

    private String httpRelativeUrl;

    private Enumerators.HttpMethod httpMethod;

    private Map<String, String> httpHeaders;

    private String mqttTopicToPublish;

    private Enumerators.MqttQosLevel mqttQosLevel;

    private Enumerators.DataType dataType;

    private String dataFormatMessageToSend;

    private String mimeType;

    private String dataUnit;

    private Long locationLat;

    private Long localtionLong;

    private Boolean showInMainDashboard;

    public Actuator() {}

    protected Actuator(Parcel in) {
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
        network = in.readParcelable(Network.class.getClassLoader());
        httpRelativeUrl = in.readString();
        httpMethod = Enumerators.HttpMethod.valueOf(in.readInt());
        int size = in.readInt();
        for(int i = 0; i < size; i++){
            String key = in.readString();
            String value = in.readString();
            httpHeaders.put(key,value);
        }
        mqttTopicToPublish = in.readString();
        dataType = Enumerators.DataType.valueOf(in.readInt());
        dataFormatMessageToSend = in.readString();
        mimeType = in.readString();
        dataUnit = in.readString();
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
        showInMainDashboard = in.readByte() != 0;
    }

    public static final Creator<Actuator> CREATOR = new Creator<Actuator>() {
        @Override
        public Actuator createFromParcel(Parcel in) {
            return new Actuator(in);
        }

        @Override
        public Actuator[] newArray(int size) {
            return new Actuator[size];
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

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network stNetwork) {
        this.network = stNetwork;
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

    public String getDataFormatMessageToSend() {
        return dataFormatMessageToSend;
    }

    public void setDataFormatMessageToSend(String sDataFormatMessageToSend) {
        this.dataFormatMessageToSend = sDataFormatMessageToSend;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDataUnit() {
        return dataUnit;
    }

    public void setDataUnit(String sDataUnit) {
        this.dataUnit = sDataUnit;
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
        parcel.writeParcelable(network, i);
        parcel.writeString(httpRelativeUrl);
        parcel.writeInt(httpMethod.ordinal());
        parcel.writeInt(httpHeaders.size());
        for(Map.Entry<String,String> entry : httpHeaders.entrySet()){
            parcel.writeString(entry.getKey());
            parcel.writeString(entry.getValue());
        }
        parcel.writeString(mqttTopicToPublish);
        parcel.writeInt(mqttQosLevel.ordinal());
        parcel.writeString(dataFormatMessageToSend);
        parcel.writeString(mimeType);
        parcel.writeString(dataUnit);
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
        parcel.writeByte((byte) (showInMainDashboard ? 1 : 0));
    }
}
