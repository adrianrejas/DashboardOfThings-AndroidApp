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
public class Actuator implements Parcelable {

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
        if (in.readByte() == 0) {
            dataNumberMinimum = null;
        } else {
            dataNumberMinimum = in.readFloat();
        }
        if (in.readByte() == 0) {
            dataNumberMaximum = null;
        } else {
            dataNumberMaximum = in.readFloat();
        }
        dataFormatMessageToSend = in.readString();
        httpMimeType = in.readString();
        dataUnit = in.readString();
        if (in.readByte() == 0) {
            locationLat = null;
        } else {
            locationLat = in.readDouble();
        }
        if (in.readByte() == 0) {
            locationLong = null;
        } else {
            locationLong = in.readDouble();
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
        parcel.writeInt(httpMethod.ordinal());
        parcel.writeInt(httpHeaders.size());
        for(Map.Entry<String,String> entry : httpHeaders.entrySet()){
            parcel.writeString(entry.getKey());
            parcel.writeString(entry.getValue());
        }
        parcel.writeString(mqttTopicToPublish);
        parcel.writeInt(mqttQosLevel.ordinal());
        parcel.writeInt(dataType.ordinal());
        if (dataNumberMinimum == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(dataNumberMinimum);
        }
        if (dataNumberMaximum == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeFloat(dataNumberMaximum);
        }
        parcel.writeString(dataFormatMessageToSend);
        parcel.writeString(httpMimeType);
        parcel.writeString(dataUnit);
        if (locationLat == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(locationLat);
        }
        if (locationLong == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeDouble(locationLong);
        }
        parcel.writeByte((byte) (showInMainDashboard ? 1 : 0));
    }
}
