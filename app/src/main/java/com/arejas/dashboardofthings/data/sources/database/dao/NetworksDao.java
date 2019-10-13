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
            "AND `logs`.elementType=0 AND `logs`.logLevel=1 AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs FROM networks")
    public abstract LiveData<List<NetworkExtended>> getAllExtended();

    @Query("SELECT networks.*, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=networks.id " +
            "AND `logs`.elementType=0 AND `logs`.logLevel=1 AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs FROM networks WHERE id=:id LIMIT 1")
    public abstract LiveData<NetworkExtended> findExtendedById(int id);

    @Insert
    public abstract void insert(Network network);

    @Insert
    public abstract void insertAll(Network... networks);

    @Delete
    public abstract void delete(Network network);

    @Delete
    public abstract void deteleAll(Network... networks);

    @Update
    public abstract void update(Network network);

    @Query("UPDATE `logs` SET elementName=:name WHERE id=:id AND elementType=:type")
    protected abstract void updateLogElementName(Integer id, Enumerators.ElementType type, String name);

    @Query("DELETE FROM `logs` WHERE id=:id AND elementType=:type")
    protected abstract void deleteLogsForElement(Integer id, Enumerators.ElementType type);

    @Transaction
    public void updateExtended(Network network) {
        update(network);
        updateLogElementName(network.getId(), Enumerators.ElementType.NETWORK, network.getName());
    }

    @Transaction
    public void deleteExtended(Network network) {
        delete(network);
        deleteLogsForElement(network.getId(), Enumerators.ElementType.NETWORK);
    }

}
