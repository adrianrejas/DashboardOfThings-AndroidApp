package com.arejas.dashboardofthings.data.sources.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
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
            "AND `logs`.elementType=2 AND `logs`.logLevel=1 AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs " +
            "FROM actuators, networks WHERE actuators.id=:id AND actuators.networkId= networks.id LIMIT 1")
    public abstract LiveData<ActuatorExtended> findByIdExtended(int id);

    @Query("SELECT actuators.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=actuators.id " +
            "AND `logs`.elementType=2 AND `logs`.logLevel=1 AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs " +
            "FROM actuators, networks")
    public abstract LiveData<List<ActuatorExtended>> getAllExtended();

    @Query("SELECT actuators.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=actuators.id " +
            "AND `logs`.elementType=2 AND `logs`.logLevel=1 AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs FROM actuators, networks WHERE showInMainDashboard=1")
    public abstract LiveData<List<ActuatorExtended>> getAllExtendedToBeShownInMainDashboard();

    @Query("SELECT actuators.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=actuators.id " +
            "AND `logs`.elementType=2 AND `logs`.logLevel=1 AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs FROM actuators, networks WHERE locationLat IS NOT NULL AND localtionLong IS NOT NULL")
    public abstract LiveData<List<ActuatorExtended>> getAllExtendedLocated();

    @Insert
    public abstract void insert(Actuator actuator);

    @Insert
    public abstract void insertAll(Actuator... actuators);

    @Delete
    public abstract void delete(Actuator actuator);

    @Delete
    public abstract void deteleAll(Actuator... actuators);

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

}
