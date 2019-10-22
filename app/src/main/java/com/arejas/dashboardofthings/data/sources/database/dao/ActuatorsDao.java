package com.arejas.dashboardofthings.data.sources.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.List;

@Dao
public abstract class ActuatorsDao {

    @Query("SELECT * FROM actuators")
    public abstract LiveData<List<Actuator>> getAll();

    @Query("SELECT * FROM actuators WHERE networkId=:networkId")
    public abstract LiveData<List<Actuator>> getAllFromSameNetwork(int networkId);

    @Query("SELECT * FROM actuators WHERE showInMainDashboard=1")
    public abstract LiveData<List<Actuator>> getAllToBeShownInMainDashboard();

    @Query("SELECT * FROM actuators WHERE locationLat IS NOT NULL AND localtionLong IS NOT NULL")
    public abstract LiveData<List<Actuator>> getAllLocated();

    @Query("SELECT * FROM actuators WHERE id=:id LIMIT 1")
    public abstract LiveData<Actuator> findById(int id);

    @Query("SELECT actuators.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=actuators.id " +
            "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
            "AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs " +
            "FROM actuators, networks WHERE actuators.id=:id AND actuators.networkId= networks.id LIMIT 1")
    public abstract LiveData<ActuatorExtended> findByIdExtended(int id,
                                                                Enumerators.ElementType[] elementTypes,
                                                                Enumerators.LogLevel[] logLevels);

    @Query("SELECT actuators.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=actuators.id " +
            "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
            "AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs " +
            "FROM actuators, networks")
    public abstract LiveData<List<ActuatorExtended>> getAllExtended(Enumerators.ElementType[] elementTypes,
                                                                       Enumerators.LogLevel[] logLevels);

    @Query("SELECT actuators.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=actuators.id " +
            "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
            "AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs FROM actuators, networks WHERE showInMainDashboard=1")
    public abstract LiveData<List<ActuatorExtended>> getAllExtendedToBeShownInMainDashboard(Enumerators.ElementType[] elementTypes,
                                                                                               Enumerators.LogLevel[] logLevels);

    @Query("SELECT actuators.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=actuators.id " +
            "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
            "AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs FROM actuators, networks WHERE locationLat IS NOT NULL AND localtionLong IS NOT NULL")
    public abstract LiveData<List<ActuatorExtended>> getAllExtendedLocated(Enumerators.ElementType[] elementTypes,
                                                                              Enumerators.LogLevel[] logLevels);

    @Insert
    public abstract void insert(Actuator actuator);

    @Insert
    public abstract void insertAll(Actuator... actuators);

    @Delete
    public abstract void delete(Actuator actuator);

    @Delete
    public abstract void deteleAll(Actuator... actuators);

    @Query("DELETE FROM networks WHERE id=:id")
    protected abstract void deleteById(Integer id);

    @Update
    public abstract void update(Actuator actuator);

    @Query("UPDATE `logs` SET elementName=:name WHERE id=:id AND elementType=:type")
    protected abstract void updateLogElementName(Integer id, Enumerators.ElementType type, String name);

    @Query("DELETE FROM `logs` WHERE id=:id AND elementType=:type")
    protected abstract void deleteLogsForElement(Integer id, Enumerators.ElementType type);

    @Transaction
    public void updateExtended(Actuator actuator) {
        update(actuator);
        updateLogElementName(actuator.getId(), Enumerators.ElementType.ACTUATOR, actuator.getName());
    }

    @Transaction
    public void deleteExtended(Actuator actuator) {
        delete(actuator);
        deleteLogsForElement(actuator.getId(), Enumerators.ElementType.ACTUATOR);
    }

    @Transaction
    public void deleteExtended(Integer id) {
        deleteById(id);
        deleteLogsForElement(id, Enumerators.ElementType.ACTUATOR);
    }

}
