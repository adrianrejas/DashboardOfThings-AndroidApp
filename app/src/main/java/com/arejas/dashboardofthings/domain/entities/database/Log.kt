package com.arejas.dashboardofthings.domain.entities.database

import android.os.Parcel
import android.os.Parcelable

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

import com.arejas.dashboardofthings.utils.Enumerators

import java.util.Date

@Entity(tableName = "logs", indices = [Index(value = ["elementId", "dateRegistered"])])
class Log {

    @PrimaryKey(autoGenerate = true)
    var id: Int = -1

    var elementId: Int = -1

    lateinit var elementName: String

    lateinit var elementType: Enumerators.ElementType

    lateinit var logMessage: String

    lateinit var logLevel: Enumerators.LogLevel

    lateinit var dateRegistered: Date

}
