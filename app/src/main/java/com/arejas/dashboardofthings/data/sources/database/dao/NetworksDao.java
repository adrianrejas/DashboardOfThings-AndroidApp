package com.arejas.dashboardofthings.data.sources.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.List;

@Dao
public abstract class NetworksDao {

    @Query("SELECT * FROM networks")
    public abstract List<Network> getAllBlocking();

    @Query("SELECT * FROM networks")
    public abstract LiveData<List<Network>> getAll();

    @Query("SELECT * FROM networks WHERE id=:id LIMIT 1")
    public abstract LiveData<Network> findById(int id);

    @Query("SELECT networks.*, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=networks.id " +
            "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
            "AND `logs`.dateRegistered >= CAST(strftime('%s', 'now') AS LONG)*1000-300000) " +
            "AS recentErrorLogs FROM networks")
    public abstract LiveData<List<NetworkExtended>> getAllExtended(Enumerators.ElementType[] elementTypes,
                                                                   Enumerators.LogLevel[] logLevels);

    @Query("SELECT networks.*, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=networks.id " +
            "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
            "AND `logs`.dateRegistered >= CAST(strftime('%s', 'now') AS LONG)*1000-300000) " +
            "AS recentErrorLogs FROM networks WHERE id=:id LIMIT 1")
    public abstract LiveData<NetworkExtended> findExtendedById(int id,
                                                               Enumerators.ElementType[] elementTypes,
                                                               Enumerators.LogLevel[] logLevels);

    @Insert
    public abstract long insert(Network network);

    @Insert
    public abstract void insertAll(Network... networks);

    @Delete
    public abstract void delete(Network network);

    @Delete
    public abstract void deteleAll(Network... networks);

    @Query("DELETE FROM networks WHERE id=:id")
    protected abstract void deleteById(Integer id);

    @Update
    public abstract void update(Network network);

    @Query("DELETE FROM `logs` WHERE id=:id AND elementType=:type")
    public abstract void deleteLogsForElement(Integer id, Enumerators.ElementType type);

    @Transaction
    public void deleteExtended(Network network) {
        delete(network);
        deleteLogsForElement(network.getId(), Enumerators.ElementType.NETWORK);
    }

    @Transaction
    public void deleteExtended(Integer id) {
        deleteById(id);
        deleteLogsForElement(id, Enumerators.ElementType.NETWORK);
    }

}
