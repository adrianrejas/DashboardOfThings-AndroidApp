package com.arejas.dashboardofthings.data.sources.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

import com.arejas.dashboardofthings.domain.entities.database.DataValue

import java.util.Calendar
import java.util.Date

@Dao
abstract class DataValuesDao {

    @get:Query("SELECT * FROM `values`")
    abstract val all: LiveData<List<DataValue>>

    @get:Query("SELECT `values`.id, `values`.sensorId, `values`.value, " + "MAX(`values`.dateReceived) AS dateReceived FROM `values` GROUP BY `values`.sensorId")
    abstract val lastValuesForAll: LiveData<List<DataValue>>

    @get:Query(
        "SELECT `values`.id, `values`.sensorId, `values`.value, " +
                "MAX(`values`.dateReceived) AS dateReceived FROM `values` " +
                "INNER JOIN sensors ON sensors.id=`values`.sensorId " +
                "WHERE sensors.showInMainDashboard " +
                "GROUP BY `values`.sensorId"
    )
    abstract val lastValuesForAllInMainDashboard: LiveData<List<DataValue>>

    @Query("SELECT * FROM `values` WHERE sensorId=:id ORDER BY dateReceived DESC LIMIT 1")
    abstract fun findLastForSensorId(id: Int): LiveData<DataValue>

    @Query("SELECT * FROM `values` WHERE sensorId=:id ORDER BY dateReceived DESC LIMIT 1")
    abstract fun findLastForSensorIdInstant(id: Int): DataValue

    @Query("SELECT `values`.id, `values`.sensorId, `values`.value, " + "MAX(`values`.dateReceived) AS dateReceived FROM `values` WHERE sensorId IN(:ids) GROUP BY `values`.sensorId")
    abstract fun findLastForSensorIds(ids: IntArray): LiveData<List<DataValue>>

    @Query("SELECT * FROM `values` WHERE sensorId=:id ORDER BY dateReceived DESC LIMIT 20")
    abstract fun getLastValuesForSensorId(id: Int): LiveData<List<DataValue>>

    @Query(
        "SELECT `values`.id, `values`.sensorId, " +
                "cast(avg(cast(`values`.value AS FLOAT)) AS STRING) as value, " +
                "max(`values`.dateReceived) as dateReceived " +
                "FROM `values` " +
                "WHERE sensorId=:id AND dateReceived >= :date " +
                "GROUP BY strftime('%H', datetime(`values`.dateReceived/1000, 'unixepoch'))"
    )
    abstract fun getAvgHourValuesForSensorId(id: Int, date: Date): LiveData<List<DataValue>>

    @Query(
        "SELECT `values`.id, `values`.sensorId, " +
                "cast(avg(cast(`values`.value AS FLOAT)) AS STRING) as value, " +
                "max(`values`.dateReceived) as dateReceived " +
                "FROM `values` " +
                "WHERE sensorId=:id AND dateReceived >= :date " +
                "GROUP BY strftime('%w', datetime(`values`.dateReceived/1000, 'unixepoch'))"
    )
    abstract fun getAvgWeekdayValuesForSensorId(id: Int, date: Date): LiveData<List<DataValue>>

    @Query(
        "SELECT `values`.id, `values`.sensorId, " +
                "cast(avg(cast(`values`.value AS FLOAT)) AS STRING) as value, " +
                "max(`values`.dateReceived) as dateReceived " +
                "FROM `values` " +
                "WHERE sensorId=:id AND dateReceived >= :date " +
                "GROUP BY strftime('%d', datetime(`values`.dateReceived/1000, 'unixepoch'))"
    )
    abstract fun getAvgPerMonthDayValuesForSensorId(id: Int, date: Date): LiveData<List<DataValue>>

    @Query(
        "SELECT `values`.id, `values`.sensorId, " +
                "cast(avg(cast(`values`.value AS FLOAT)) AS STRING) as value, " +
                "max(`values`.dateReceived) as dateReceived " +
                "FROM `values` " +
                "WHERE sensorId=:id AND dateReceived >= :date " +
                "GROUP BY strftime('%m', datetime(`values`.dateReceived/1000, 'unixepoch'))"
    )
    abstract fun getAvgPerMonthValuesForSensorId(id: Int, date: Date): LiveData<List<DataValue>>

    @Query("SELECT * FROM `values` WHERE sensorId=:id AND dateReceived >= date('now','-1 year')")
    abstract fun getAllLastOneYearValuesForSensorId(id: Int): LiveData<List<DataValue>>

    @Insert
    protected abstract fun insertInner(value: DataValue)

    @Insert
    protected abstract fun insertAllInner(vararg values: DataValue)

    @Transaction
    fun insert(value: DataValue) {
        insertInner(value)
        val cal = Calendar.getInstance()
        val today = cal.time
        cal.add(Calendar.YEAR, -1)
        val prevYear = cal.time
        deleteOldValues(prevYear)
    }

    @Transaction
    fun insertAll(vararg values: DataValue) {
        insertAllInner(*values)
        val cal = Calendar.getInstance()
        val today = cal.time
        cal.add(Calendar.YEAR, -1)
        val prevYear = cal.time
        deleteOldValues(prevYear)
    }

    @Delete
    protected abstract fun delete(value: DataValue)

    @Delete
    protected abstract fun deteleAll(vararg values: DataValue)

    @Query("DELETE FROM `values` WHERE dateReceived <= :date")
    protected abstract fun deleteOldValues(date: Date)
}
