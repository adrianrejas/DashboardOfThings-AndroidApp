package com.arejas.dashboardofthings.domain.entities.database;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.arejas.dashboardofthings.utils.Enumerators;

@Entity(tableName = "networks")
public class Network {

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

    public static class HttpNetworkParameters {

        private String httpBaseUrl;

        private Enumerators.HttpAuthenticationType httpAauthenticationType;

        private String httpUsername;

        private String httpPassword;

        private Boolean httpUseSsl;

        private String certAuthorityUri;

        public HttpNetworkParameters() {}

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

    public static class MqttNetworkParameters {

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
