package com.arejas.dashboardofthings.domain.entities.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.arejas.dashboardofthings.utils.Enumerators;

@Entity(tableName = "networks")
public class Network implements Parcelable {

    @PrimaryKey (autoGenerate = true)
    private Integer id;

    private String name;

    private Enumerators.NetworkType networkType;

    private String imageUri;

    @Embedded
    private HttpNetworkParameters httpConfiguration;

    @Embedded
    private MqttNetworkParameters mqttConfiguration;

    public Network() {}

    protected Network(Parcel in) {
        id = in.readInt();
        name = in.readString();
        networkType = Enumerators.NetworkType.valueOf(in.readInt());
        imageUri = in.readString();
        httpConfiguration = in.readParcelable(HttpNetworkParameters.class.getClassLoader());
        mqttConfiguration = in.readParcelable(MqttNetworkParameters.class.getClassLoader());
    }

    public static final Creator<Network> CREATOR = new Creator<Network>() {
        @Override
        public Network createFromParcel(Parcel in) {
            return new Network(in);
        }

        @Override
        public Network[] newArray(int size) {
            return new Network[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeInt(networkType.ordinal());
        parcel.writeString(imageUri);
        parcel.writeParcelable(httpConfiguration, i);
        parcel.writeParcelable(mqttConfiguration, i);
    }

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

    public Enumerators.NetworkType getNetworkType() {
        return networkType;
    }

    public void setNetworkType(Enumerators.NetworkType eNetworkType) {
        this.networkType = eNetworkType;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String sImageUri) {
        this.imageUri = sImageUri;
    }

    public HttpNetworkParameters getHttpConfiguration() {
        return httpConfiguration;
    }

    public void setHttpConfiguration(HttpNetworkParameters httpConfiguration) {
        this.httpConfiguration = httpConfiguration;
    }

    public MqttNetworkParameters getMqttConfiguration() {
        return mqttConfiguration;
    }

    public void setMqttConfiguration(MqttNetworkParameters mqttConfiguration) {
        this.mqttConfiguration = mqttConfiguration;
    }

    public static class HttpNetworkParameters implements Parcelable {

        private String httpBaseUrl;

        private Enumerators.HttpAuthenticationType httpAauthenticationType;

        private String httpUsername;

        private String httpPassword;

        private Boolean httpUseSsl;

        private String certAuthorityUri;

        public HttpNetworkParameters() {}

        protected HttpNetworkParameters(Parcel in) {
            httpBaseUrl = in.readString();
            httpAauthenticationType = Enumerators.HttpAuthenticationType.valueOf(in.readInt());
            httpUsername = in.readString();
            httpPassword = in.readString();
            byte tmpBUsesSsl = in.readByte();
            httpUseSsl = tmpBUsesSsl == 0 ? null : tmpBUsesSsl == 1;
            certAuthorityUri = in.readString();
        }

        public static final Creator<HttpNetworkParameters> CREATOR = new Creator<HttpNetworkParameters>() {
            @Override
            public HttpNetworkParameters createFromParcel(Parcel in) {
                return new HttpNetworkParameters(in);
            }

            @Override
            public HttpNetworkParameters[] newArray(int size) {
                return new HttpNetworkParameters[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(httpBaseUrl);
            parcel.writeInt(httpAauthenticationType.ordinal());
            parcel.writeString(httpUsername);
            parcel.writeString(httpPassword);
            parcel.writeByte((byte) (httpUseSsl == null ? 0 : httpUseSsl ? 1 : 2));
            parcel.writeString(certAuthorityUri);
        }

        public String getHttpBaseUrl() {
            return httpBaseUrl;
        }

        public void setHttpBaseUrl(String sBaseUrl) {
            this.httpBaseUrl = sBaseUrl;
        }

        public Enumerators.HttpAuthenticationType getHttpAauthenticationType() {
            return httpAauthenticationType;
        }

        public void setHttpAauthenticationType(Enumerators.HttpAuthenticationType eAuthenticationType) {
            this.httpAauthenticationType = eAuthenticationType;
        }

        public String getHttpUsername() {
            return httpUsername;
        }

        public void setHttpUsername(String sUsername) {
            this.httpUsername = sUsername;
        }

        public String getHttpPassword() {
            return httpPassword;
        }

        public void setHttpPassword(String sPassword) {
            this.httpPassword = sPassword;
        }

        public Boolean getHttpUseSsl() {
            return httpUseSsl;
        }

        public void setHttpUseSsl(Boolean bUsesSsl) {
            this.httpUseSsl = bUsesSsl;
        }

        public String getCertAuthorityUri() {
            return certAuthorityUri;
        }

        public void setCertAuthorityUri(String sCertAuthorityUrl) {
            this.certAuthorityUri = sCertAuthorityUrl;
        }
        
    }

    public static class MqttNetworkParameters implements Parcelable {

        private String mqttBrokerUrl;

        private String mqttClientId;

        private String mqttUsername;

        private String mqttPassword;

        private Boolean mqttCleanSession;

        private Integer mqttConnTimeout;

        private Integer mqttKeepaliveInterval;

        private Boolean mqttUseSsl;

        private String mqttCertAuthorityUri;

        public MqttNetworkParameters() {}

        protected MqttNetworkParameters(Parcel in) {
            mqttBrokerUrl = in.readString();
            mqttClientId = in.readString();
            mqttUsername = in.readString();
            mqttPassword = in.readString();
            byte tmpBCleanSession = in.readByte();
            mqttCleanSession = tmpBCleanSession == 0 ? null : tmpBCleanSession == 1;
            if (in.readByte() == 0) {
                mqttConnTimeout = null;
            } else {
                mqttConnTimeout = in.readInt();
            }
            if (in.readByte() == 0) {
                mqttKeepaliveInterval = null;
            } else {
                mqttKeepaliveInterval = in.readInt();
            }
            byte tmpBUsesSsl = in.readByte();
            mqttUseSsl = tmpBUsesSsl == 0 ? null : tmpBUsesSsl == 1;
            mqttCertAuthorityUri = in.readString();
        }

        public static final Creator<MqttNetworkParameters> CREATOR = new Creator<MqttNetworkParameters>() {
            @Override
            public MqttNetworkParameters createFromParcel(Parcel in) {
                return new MqttNetworkParameters(in);
            }

            @Override
            public MqttNetworkParameters[] newArray(int size) {
                return new MqttNetworkParameters[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(mqttBrokerUrl);
            parcel.writeString(mqttClientId);
            parcel.writeString(mqttUsername);
            parcel.writeString(mqttPassword);
            parcel.writeByte((byte) (mqttCleanSession == null ? 0 : mqttCleanSession ? 1 : 2));
            if (mqttConnTimeout == null) {
                parcel.writeByte((byte) 0);
            } else {
                parcel.writeByte((byte) 1);
                parcel.writeInt(mqttConnTimeout);
            }
            if (mqttKeepaliveInterval == null) {
                parcel.writeByte((byte) 0);
            } else {
                parcel.writeByte((byte) 1);
                parcel.writeInt(mqttKeepaliveInterval);
            }
            parcel.writeByte((byte) (mqttUseSsl == null ? 0 : mqttUseSsl ? 1 : 2));
            parcel.writeString(mqttCertAuthorityUri);
        }

        public String getMqttBrokerUrl() {
            return mqttBrokerUrl;
        }

        public void setMqttBrokerUrl(String sBrokerUrl) {
            this.mqttBrokerUrl = sBrokerUrl;
        }

        public String getMqttClientId() {
            return mqttClientId;
        }

        public void setMqttClientId(String mqttClientId) {
            this.mqttClientId = mqttClientId;
        }

        public String getMqttUsername() {
            return mqttUsername;
        }

        public void setMqttUsername(String sUsername) {
            this.mqttUsername = sUsername;
        }

        public String getMqttPassword() {
            return mqttPassword;
        }

        public void setMqttPassword(String sPassword) {
            this.mqttPassword = sPassword;
        }

        public Boolean getMqttCleanSession() {
            return mqttCleanSession;
        }

        public void setMqttCleanSession(Boolean bCleanSession) {
            this.mqttCleanSession = bCleanSession;
        }

        public Integer getMqttConnTimeout() {
            return mqttConnTimeout;
        }

        public void setMqttConnTimeout(Integer iConnTimeout) {
            this.mqttConnTimeout = iConnTimeout;
        }

        public Integer getMqttKeepaliveInterval() {
            return mqttKeepaliveInterval;
        }

        public void setMqttKeepaliveInterval(Integer iKeepaliveInterval) {
            this.mqttKeepaliveInterval = iKeepaliveInterval;
        }

        public Boolean getMqttUseSsl() {
            return mqttUseSsl;
        }

        public void setMqttUseSsl(Boolean bUsesSsl) {
            this.mqttUseSsl = bUsesSsl;
        }

        public String getMqttCertAuthorityUri() {
            return mqttCertAuthorityUri;
        }

        public void setMqttCertAuthorityUri(String sCertAuthorityUrl) {
            this.mqttCertAuthorityUri = sCertAuthorityUrl;
        }

    }

}
