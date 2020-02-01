package com.arejas.dashboardofthings.domain.entities.database

import android.os.Parcel
import android.os.Parcelable

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

import com.arejas.dashboardofthings.utils.Enumerators

@Entity(tableName = "networks")
open class Network {

    @PrimaryKey(autoGenerate = true)
    var id: Int = -1

    lateinit var name: String

    lateinit var networkType: Enumerators.NetworkType

    var imageUri: String? = null

    @Embedded
    var httpConfiguration: HttpNetworkParameters? = null

    @Embedded
    var mqttConfiguration: MqttNetworkParameters? = null

    class HttpNetworkParameters {

        var httpBaseUrl: String? = null

        var httpAauthenticationType: Enumerators.HttpAuthenticationType? = null

        var httpUsername: String? = null

        var httpPassword: String? = null

        var httpUseSsl: Boolean? = null

        var certAuthorityUri: String? = null

    }

    class MqttNetworkParameters {

        var mqttBrokerUrl: String? = null

        var mqttClientId: String? = null

        var mqttUsername: String? = null

        var mqttPassword: String? = null

        var mqttCleanSession: Boolean? = null

        var mqttConnTimeout: Int? = null

        var mqttKeepaliveInterval: Int? = null

        var mqttUseSsl: Boolean? = null

        var mqttCertAuthorityUri: String? = null

    }

}
