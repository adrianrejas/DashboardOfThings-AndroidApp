package com.arejas.dashboardofthings.data.sources.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

import com.arejas.dashboardofthings.domain.entities.database.Log
import com.arejas.dashboardofthings.utils.Enumerators

@Dao
abstract class LogsDao {

    @get:Query("SELECT * FROM `logs`")
    abstract val all: LiveData<List<Log>>

    @get:Query("SELECT * FROM `logs` ORDER BY dateRegistered DESC LIMIT 100")
    abstract val allLastHundredLogs: LiveData<List<Log>>

    @get:Query("SELECT * FROM `logs` WHERE dateRegistered IN (SELECT MAX(dateRegistered) FROM `logs` GROUP BY ElementId, elementType) ORDER BY dateRegistered DESC")
    abstract val lastLogForEachElement: LiveData<List<Log>>

    @Query("SELECT * FROM `logs` WHERE logLevel IN(:logLevels) ORDER BY dateRegistered DESC LIMIT 100")
    abstract fun getAllLastHundredLogs(logLevels: Array<Enumerators.LogLevel>): LiveData<List<Log>>

    @Query("SELECT * FROM `logs` WHERE elementId=:id AND elementType=:elementType ORDER BY dateRegistered DESC LIMIT 1")
    abstract fun findLastForElementId(id: Int, elementType: Enumerators.ElementType): LiveData<Log>

    @Query("SELECT * FROM `logs` WHERE elementId=:id AND elementType=:elementType " + "AND logLevel IN(:logLevels) LIMIT 1")
    abstract fun findLastForElementId(
        id: Int, elementType: Enumerators.ElementType,
        logLevels: Array<Enumerators.LogLevel>
    ): LiveData<Log>

    @Query("SELECT * FROM `logs` WHERE elementId=:id AND elementType=:elementType ORDER BY dateRegistered DESC LIMIT 100")
    abstract fun getLastHundredLogsForElementId(
        id: Int,
        elementType: Enumerators.ElementType
    ): LiveData<List<Log>>

    @Query("SELECT * FROM `logs` WHERE dateRegistered IN (SELECT MAX(dateRegistered) FROM `logs` WHERE elementType IN(:elementTypes) " + "GROUP BY ElementId, elementType) ORDER BY dateRegistered DESC")
    abstract fun getLastLogForEachElement(elementTypes: Array<Enumerators.ElementType>): LiveData<List<Log>>

    @Query("SELECT * FROM `logs` WHERE dateRegistered IN (SELECT MAX(dateRegistered) FROM `logs` " + "WHERE logLevel IN(:logLevels) GROUP BY ElementId, elementType) ORDER BY dateRegistered DESC")
    abstract fun getLastLogForEachElement(logLevels: Array<Enumerators.LogLevel>): LiveData<List<Log>>

    @Query(
        "SELECT * FROM `logs` WHERE dateRegistered IN (SELECT MAX(dateRegistered) FROM `logs` " +
                "WHERE elementType IN(:elementTypes) AND logLevel IN(:logLevels) " +
                "GROUP BY ElementId, elementType) ORDER BY dateRegistered DESC"
    )
    abstract fun getLastLogForEachElement(
        elementTypes: Array<Enumerators.ElementType>,
        logLevels: Array<Enumerators.LogLevel>
    ): LiveData<List<Log>>

    @Query(
        "SELECT * FROM `logs` INNER JOIN sensors ON (sensors.id=logs.elementId AND sensors.showInMainDashboard)" +
                "WHERE dateRegistered IN (SELECT MAX(dateRegistered) FROM `logs` " +
                "WHERE elementType=1 AND logLevel IN(:logLevels) GROUP BY ElementId) ORDER BY dateRegistered DESC"
    )
    abstract fun getLastLogForSensorElementsInMainDashboard(logLevels: Array<Enumerators.LogLevel>): LiveData<List<Log>>

    @Insert
    protected abstract fun insertInner(log: Log)

    @Insert
    protected abstract fun insertAllInner(vararg logs: Log)

    @Transaction
    fun insert(log: Log) {
        insertInner(log)
        deleteOldValues()
    }

    @Transaction
    fun insertAll(vararg logs: Log) {
        insertAllInner(*logs)
        deleteOldValues()
    }

    @Query("UPDATE `logs` SET elementName=:name WHERE id=:id AND elementType=:type")
    abstract fun updateElementName(id: Int?, type: Enumerators.ElementType, name: String)

    @Delete
    protected abstract fun delete(log: Log)

    @Query("DELETE FROM `logs`")
    abstract fun deleteAll()

    @Delete
    protected abstract fun deteleAll(vararg logs: Log)

    @Query("DELETE FROM `logs` WHERE id=:id AND elementType=:type")
    abstract fun deleteAll(id: Int?, type: Enumerators.ElementType)

    @Query("DELETE FROM `logs` where id NOT IN (SELECT id from `logs` ORDER BY dateRegistered DESC LIMIT 10000)")
    protected abstract fun deleteOldValues()

}
