package com.arejas.dashboardofthings.data.sources.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.widget.SensorWidgetItem
import com.arejas.dashboardofthings.presentation.ui.widget.SensorWidget
import com.arejas.dashboardofthings.utils.Enumerators

@Dao
abstract class SensorsDao {

    @get:Query("SELECT * FROM sensors")
    abstract val allBlocking: List<Sensor>

    @get:Query("SELECT * FROM sensors")
    abstract val all: LiveData<List<Sensor>>

    @get:Query("SELECT * FROM sensors WHERE isShowInMainDashboard=1")
    abstract val allToBeShownInMainDashboard: LiveData<List<Sensor>>

    @get:Query("SELECT * FROM sensors WHERE locationLat IS NOT NULL AND locationLong IS NOT NULL")
    abstract val allLocated: LiveData<List<Sensor>>

    @Query("SELECT * FROM sensors WHERE networkId=:networkId")
    abstract fun getAllFromSameNetwork(networkId: Int): LiveData<List<Sensor>>

    @Query("SELECT * FROM sensors WHERE id=:id LIMIT 1")
    abstract fun findById(id: Int): LiveData<Sensor>

    @Query("SELECT * FROM sensors WHERE id=:id LIMIT 1")
    abstract fun findByIdInstant(id: Int): Sensor

    @Query(
        "SELECT sensors.id AS sensorId, sensors.name AS sensorName, sensors.type AS sensorType, " +
                "sensors.dataType AS sensorDataType, sensors.dataUnit AS sensorUnit," +
                "(SELECT `values`.value FROM `values` " +
                "WHERE  `values`.sensorId=:id ORDER BY dateReceived DESC LIMIT 1) AS lastValueReceived " +
                "FROM sensors WHERE sensors.id=:id LIMIT 1"
    )
    abstract fun findByIdForWidgetInstant(id: Int): SensorWidgetItem

    @Query(
        "SELECT sensors.id AS sensorId, sensors.name AS sensorName, sensors.type AS sensorType, " +
                "sensors.dataType AS sensorDataType, sensors.dataUnit AS sensorUnit, " +
                "lastValues.value AS lastValueReceived FROM sensors " +
                "LEFT JOIN (SELECT `values`.sensorId, `values`.value, MAX(`values`.dateReceived) AS dateReceived " +
                "FROM `values` GROUP BY `values`.sensorId) as lastValues ON lastValues.sensorId = sensors.id " +
                "WHERE sensors.id IN(:ids)"
    )
    abstract fun getAllForWidgetsInstant(ids: IntArray): List<SensorWidgetItem>

    @Query(
        "SELECT sensors.*, networks.name AS networkName, networks.networkType AS networkType, " +
                "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=sensors.id " +
                "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
                "AND `logs`.dateRegistered >= CAST(strftime('%s', 'now') AS LONG)*1000-300000) " +
                "AS recentErrorLogs " +
                "FROM sensors " +
                "INNER JOIN networks ON sensors.networkId=networks.id"
    )
    abstract fun getAllExtended(
        elementTypes: Array<Enumerators.ElementType>,
        logLevels: Array<Enumerators.LogLevel>
    ): LiveData<List<SensorExtended>>

    @Query(
        "SELECT sensors.*, networks.name AS networkName, networks.networkType AS networkType, " +
                "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=sensors.id " +
                "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
                "AND `logs`.dateRegistered >= CAST(strftime('%s', 'now') AS LONG)*1000-300000) " +
                "AS recentErrorLogs " +
                "FROM sensors " +
                "INNER JOIN networks ON sensors.networkId=networks.id " +
                "WHERE showInMainDashboard=1"
    )
    abstract fun getAllExtendedToBeShownInMainDashboard(
        elementTypes: Array<Enumerators.ElementType>,
        logLevels: Array<Enumerators.LogLevel>
    ): LiveData<List<SensorExtended>>

    @Query(
        "SELECT sensors.*, networks.name AS networkName, networks.networkType AS networkType, " +
                "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=sensors.id " +
                "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
                "AND `logs`.dateRegistered >= CAST(strftime('%s', 'now') AS LONG)*1000-300000) " +
                "AS recentErrorLogs " +
                "FROM sensors " +
                "INNER JOIN networks ON sensors.networkId=networks.id " +
                "WHERE locationLat IS NOT NULL AND locationLong IS NOT NULL"
    )
    abstract fun getAllExtendedLocated(
        elementTypes: Array<Enumerators.ElementType>,
        logLevels: Array<Enumerators.LogLevel>
    ): LiveData<List<SensorExtended>>

    @Query(
        "SELECT sensors.*, networks.name AS networkName, networks.networkType AS networkType, " +
                "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=sensors.id " +
                "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
                "AND `logs`.dateRegistered >= CAST(strftime('%s', 'now') AS LONG)*1000-300000) " +
                "AS recentErrorLogs " +
                "FROM sensors " +
                "INNER JOIN networks ON sensors.networkId=networks.id " +
                "WHERE sensors.id=:id LIMIT 1"
    )
    abstract fun findByIdExtended(
        id: Int,
        elementTypes: Array<Enumerators.ElementType>,
        logLevels: Array<Enumerators.LogLevel>
    ): LiveData<SensorExtended>

    @Insert
    abstract fun insert(sensor: Sensor): Long

    @Insert
    abstract fun insertAll(vararg sensors: Sensor)

    @Delete
    abstract fun delete(sensor: Sensor)

    @Delete
    abstract fun deteleAll(vararg sensors: Sensor)

    @Query("DELETE FROM networks WHERE id=:id")
    protected abstract fun deleteById(id: Int?)

    @Update
    abstract fun update(sensor: Sensor)

    @Query("DELETE FROM `logs` WHERE id=:id AND elementType=:type")
    protected abstract fun deleteLogsForElement(id: Int?, type: Enumerators.ElementType)

    @Transaction
    fun deleteExtended(sensor: Sensor) {
        delete(sensor)
        deleteLogsForElement(sensor.id, Enumerators.ElementType.SENSOR)
    }

    @Transaction
    fun deleteExtended(id: Int?) {
        deleteById(id)
        deleteLogsForElement(id, Enumerators.ElementType.SENSOR)
    }

}
