package com.arejas.dashboardofthings.data.sources.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import com.arejas.dashboardofthings.data.sources.database.converters.DotTypeConverters
import com.arejas.dashboardofthings.data.sources.database.dao.ActuatorsDao
import com.arejas.dashboardofthings.data.sources.database.dao.DataValuesDao
import com.arejas.dashboardofthings.data.sources.database.dao.LogsDao
import com.arejas.dashboardofthings.data.sources.database.dao.NetworksDao
import com.arejas.dashboardofthings.data.sources.database.dao.SensorsDao
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.database.Log
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor

@Database(
    entities = [Network::class, Sensor::class, Actuator::class, DataValue::class, Log::class],
    version = 1
)
@TypeConverters(DotTypeConverters::class)
abstract class DotDatabase : RoomDatabase() {

    abstract fun networksDao(): NetworksDao

    abstract fun sensorsDao(): SensorsDao

    abstract fun actuatorsDao(): ActuatorsDao

    abstract fun dataValuesDao(): DataValuesDao

    abstract fun logsDao(): LogsDao

    companion object {

        val DOT_DB_NAME = "dashboard_of_things.db"
    }

}
