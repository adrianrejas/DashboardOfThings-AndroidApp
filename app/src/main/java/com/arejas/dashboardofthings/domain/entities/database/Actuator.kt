package com.arejas.dashboardofthings.domain.entities.database

import android.os.Parcel
import android.os.Parcelable

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

import com.arejas.dashboardofthings.utils.Enumerators

import androidx.room.ForeignKey.CASCADE

@Entity(
    tableName = "actuators",
    foreignKeys = [ForeignKey(
        entity = Network::class,
        parentColumns = ["id"],
        childColumns = ["networkId"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["networkId"])]
)
open class Actuator {

    @PrimaryKey(autoGenerate = true)
    var id: Int = -1

    lateinit var name: String

    lateinit var type: String

    var imageUri: String? = null

    @ColumnInfo(name = "networkId")
    var networkId: Int = -1

    var httpRelativeUrl: String? = null

    var httpMethod: Enumerators.HttpMethod? = null

    var httpHeaders: Map<String, String>? = null

    var httpMimeType: String? = null

    var mqttTopicToPublish: String? = null

    var mqttQosLevel: Enumerators.MqttQosLevel? = null

    var dataType: Enumerators.DataType? = null

    var dataNumberMinimum: Float? = null

    var dataNumberMaximum: Float? = null

    var dataFormatMessageToSend: String? = null

    var dataUnit: String? = null

    var locationLat: Double? = null

    var locationLong: Double? = null

    var isShowInMainDashboard: Boolean? = null

}
