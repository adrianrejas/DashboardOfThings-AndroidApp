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
    tableName = "sensors",
    foreignKeys = [ForeignKey(
        entity = Network::class,
        parentColumns = ["id"],
        childColumns = ["networkId"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["networkId"])]
)
open class Sensor {

    @PrimaryKey(autoGenerate = true)
    var id: Int = -1

    lateinit var name: String

    lateinit var type: String

    var imageUri: String? = null

    @ColumnInfo(name = "networkId")
    var networkId: Int = -1

    var httpRelativeUrl: String? = null

    var httpHeaders: Map<String, String>? = null

    var httpSecondsBetweenRequests: Int? = null

    var mqttTopicToSubscribe: String? = null

    var mqttQosLevel: Enumerators.MqttQosLevel? = null

    var messageType: Enumerators.MessageType? = null

    var dataType: Enumerators.DataType? = null

    var xmlOrJsonNode: String? = null

    var rawRegularExpression: String? = null

    var dataUnit: String? = null

    var thresholdAboveCritical: Float? = null

    var thresholdAboveWarning: Float? = null

    var thresholdBelowCritical: Float? = null

    var thresholdBelowWarning: Float? = null

    var thresholdEqualsWarning: String? = null

    var thresholdEqualsCritical: String? = null

    var locationLat: Double? = null

    var locationLong: Double? = null

    var isShowInMainDashboard: Boolean? = null

}
