package com.arejas.dashboardofthings.data.sources.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.utils.Enumerators

@Dao
abstract class ActuatorsDao {

    @get:Query("SELECT * FROM actuators")
    abstract val allInstant: List<Actuator>

    @get:Query("SELECT * FROM actuators")
    abstract val all: LiveData<List<Actuator>>

    @get:Query("SELECT * FROM actuators WHERE isShowInMainDashboard=1")
    abstract val allToBeShownInMainDashboard: LiveData<List<Actuator>>

    @get:Query("SELECT * FROM actuators WHERE locationLat IS NOT NULL AND locationLong IS NOT NULL")
    abstract val allLocated: LiveData<List<Actuator>>

    @Query("SELECT * FROM actuators WHERE networkId=:networkId")
    abstract fun getAllFromSameNetwork(networkId: Int): LiveData<List<Actuator>>

    @Query("SELECT * FROM actuators WHERE id=:id LIMIT 1")
    abstract fun findById(id: Int): LiveData<Actuator>

    @Query(
        "SELECT actuators.*, networks.name AS networkName, networks.networkType AS networkType, " +
                "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=actuators.id " +
                "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
                "AND `logs`.dateRegistered >= CAST(strftime('%s', 'now') AS LONG)*1000-300000) " +
                "AS recentErrorLogs " +
                "FROM actuators, networks WHERE actuators.id=:id AND actuators.networkId= networks.id LIMIT 1"
    )
    abstract fun findByIdExtended(
        id: Int,
        elementTypes: Array<Enumerators.ElementType>,
        logLevels: Array<Enumerators.LogLevel>
    ): LiveData<ActuatorExtended>

    @Query(
        "SELECT actuators.*, networks.name AS networkName, networks.networkType AS networkType, " +
                "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=actuators.id " +
                "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
                "AND `logs`.dateRegistered >= CAST(strftime('%s', 'now') AS LONG)*1000-300000) " +
                "AS recentErrorLogs " +
                "FROM actuators " +
                "INNER JOIN networks ON actuators.networkId=networks.id "
    )
    abstract fun getAllExtended(
        elementTypes: Array<Enumerators.ElementType>,
        logLevels: Array<Enumerators.LogLevel>
    ): LiveData<List<ActuatorExtended>>

    @Query(
        "SELECT actuators.*, networks.name AS networkName, networks.networkType AS networkType, " +
                "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=actuators.id " +
                "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
                "AND `logs`.dateRegistered >= CAST(strftime('%s', 'now') AS LONG)*1000-300000) " +
                "AS recentErrorLogs " +
                "FROM actuators " +
                "INNER JOIN networks ON actuators.networkId=networks.id "
    )
    abstract fun getAllExtendedInstant(
        elementTypes: Array<Enumerators.ElementType>,
        logLevels: Array<Enumerators.LogLevel>
    ): List<ActuatorExtended>

    @Query(
        "SELECT actuators.*, networks.name AS networkName, networks.networkType AS networkType, " +
                "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=actuators.id " +
                "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
                "AND `logs`.dateRegistered >= CAST(strftime('%s', 'now') AS LONG)*1000-300000) " +
                "AS recentErrorLogs " +
                "FROM actuators " +
                "INNER JOIN networks ON actuators.networkId=networks.id " +
                "WHERE showInMainDashboard=1"
    )
    abstract fun getAllExtendedToBeShownInMainDashboard(
        elementTypes: Array<Enumerators.ElementType>,
        logLevels: Array<Enumerators.LogLevel>
    ): LiveData<List<ActuatorExtended>>

    @Query(
        "SELECT actuators.*, networks.name AS networkName, networks.networkType AS networkType, " +
                "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=actuators.id " +
                "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
                "AND `logs`.dateRegistered >= CAST(strftime('%s', 'now') AS LONG)*1000-300000) " +
                "AS recentErrorLogs " +
                "FROM actuators " +
                "INNER JOIN networks ON actuators.networkId=networks.id " +
                "WHERE locationLat IS NOT NULL AND locationLong IS NOT NULL"
    )
    abstract fun getAllExtendedLocated(
        elementTypes: Array<Enumerators.ElementType>,
        logLevels: Array<Enumerators.LogLevel>
    ): LiveData<List<ActuatorExtended>>

    @Insert
    abstract fun insert(actuator: Actuator): Long

    @Insert
    abstract fun insertAll(vararg actuators: Actuator)

    @Delete
    abstract fun delete(actuator: Actuator)

    @Delete
    abstract fun deteleAll(vararg actuators: Actuator)

    @Query("DELETE FROM actuators WHERE id=:id")
    protected abstract fun deleteById(id: Int?)

    @Update
    abstract fun update(actuator: Actuator)

    @Query("DELETE FROM `logs` WHERE id=:id AND elementType=:type")
    protected abstract fun deleteLogsForElement(id: Int?, type: Enumerators.ElementType)

    @Transaction
    fun deleteExtended(actuator: Actuator) {
        delete(actuator)
        deleteLogsForElement(actuator.id, Enumerators.ElementType.ACTUATOR)
    }

    @Transaction
    fun deleteExtended(id: Int?) {
        deleteById(id)
        deleteLogsForElement(id, Enumerators.ElementType.ACTUATOR)
    }

}
