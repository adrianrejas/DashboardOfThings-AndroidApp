package com.arejas.dashboardofthings.domain.entities.database

import android.os.Parcel
import android.os.Parcelable

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

import java.util.Date

import androidx.room.ForeignKey.CASCADE

@Entity(
    tableName = "values",
    foreignKeys = [ForeignKey(
        entity = Sensor::class,
        parentColumns = ["id"],
        childColumns = ["sensorId"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["sensorId", "dateReceived"])]
)
class DataValue {

    @PrimaryKey(autoGenerate = true)
    var id: Int = -1

    @ColumnInfo(name = "sensorId")
    var sensorId: Int = -1

    lateinit var value: String

    lateinit var dateReceived: Date

}
