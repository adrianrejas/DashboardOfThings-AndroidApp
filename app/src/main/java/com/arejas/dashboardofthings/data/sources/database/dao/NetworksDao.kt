package com.arejas.dashboardofthings.data.sources.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.utils.Enumerators

@Dao
abstract class NetworksDao {

    @get:Query("SELECT * FROM networks")
    abstract val allBlocking: List<Network>

    @get:Query("SELECT * FROM networks")
    abstract val all: LiveData<List<Network>>

    @Query("SELECT * FROM networks WHERE id=:id LIMIT 1")
    abstract fun findById(id: Int): LiveData<Network>

    @Query(
        "SELECT networks.*, " +
                "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=networks.id " +
                "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
                "AND `logs`.dateRegistered >= CAST(strftime('%s', 'now') AS LONG)*1000-300000) " +
                "AS recentErrorLogs FROM networks"
    )
    abstract fun getAllExtended(
        elementTypes: Array<Enumerators.ElementType>,
        logLevels: Array<Enumerators.LogLevel>
    ): LiveData<List<NetworkExtended>>

    @Query(
        "SELECT networks.*, " +
                "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=networks.id " +
                "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
                "AND `logs`.dateRegistered >= CAST(strftime('%s', 'now') AS LONG)*1000-300000) " +
                "AS recentErrorLogs FROM networks WHERE id=:id LIMIT 1"
    )
    abstract fun findExtendedById(
        id: Int,
        elementTypes: Array<Enumerators.ElementType>,
        logLevels: Array<Enumerators.LogLevel>
    ): LiveData<NetworkExtended>

    @Insert
    abstract fun insert(network: Network): Long

    @Insert
    abstract fun insertAll(vararg networks: Network)

    @Delete
    abstract fun delete(network: Network)

    @Delete
    abstract fun deteleAll(vararg networks: Network)

    @Query("DELETE FROM networks WHERE id=:id")
    protected abstract fun deleteById(id: Int?)

    @Update
    abstract fun update(network: Network)

    @Query("DELETE FROM `logs` WHERE id=:id AND elementType=:type")
    abstract fun deleteLogsForElement(id: Int?, type: Enumerators.ElementType)

    @Transaction
    fun deleteExtended(network: Network) {
        delete(network)
        deleteLogsForElement(network.id, Enumerators.ElementType.NETWORK)
    }

    @Transaction
    fun deleteExtended(id: Int?) {
        deleteById(id)
        deleteLogsForElement(id, Enumerators.ElementType.NETWORK)
    }

}
