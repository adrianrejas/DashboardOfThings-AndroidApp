package com.arejas.dashboardofthings.data.sources.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.List;

@Dao
public abstract class SensorsDao {

    @Query("SELECT * FROM sensors")
    public abstract List<Sensor> getAllBlocking();

    @Query("SELECT * FROM sensors")
    public abstract LiveData<List<Sensor>> getAll();

    @Query("SELECT * FROM sensors WHERE networkId=:networkId")
    public abstract LiveData<List<Sensor>> getAllFromSameNetwork(int networkId);

    @Query("SELECT * FROM sensors WHERE showInMainDashboard=1")
    public abstract LiveData<List<Sensor>> getAllToBeShownInMainDashboard();

    @Query("SELECT * FROM sensors WHERE locationLat IS NOT NULL AND localtionLong IS NOT NULL")
    public abstract LiveData<List<Sensor>> getAllLocated();

    @Query("SELECT * FROM sensors WHERE id=:id LIMIT 1")
    public abstract LiveData<Sensor> findById(int id);

    @Query("SELECT sensors.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=sensors.id " +
            "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
            "AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs " +
            "FROM sensors " +
            "INNER JOIN networks ON sensors.networkId=networks.id")
    public abstract LiveData<List<SensorExtended>> getAllExtended(Enumerators.ElementType[] elementTypes,
                                                                     Enumerators.LogLevel[] logLevels);

    @Query("SELECT sensors.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=sensors.id " +
            "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
            "AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs " +
            "FROM sensors " +
            "INNER JOIN networks ON sensors.networkId=networks.id " +
            "WHERE showInMainDashboard=1")
    public abstract LiveData<List<SensorExtended>> getAllExtendedToBeShownInMainDashboard(Enumerators.ElementType[] elementTypes,
                                                                                          Enumerators.LogLevel[] logLevels);

    @Query("SELECT sensors.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=sensors.id " +
            "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
            "AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs " +
            "FROM sensors " +
            "INNER JOIN networks ON sensors.networkId=networks.id " +
            "WHERE locationLat IS NOT NULL AND localtionLong IS NOT NULL")
    public abstract LiveData<List<SensorExtended>> getAllExtendedLocated(Enumerators.ElementType[] elementTypes,
                                                                         Enumerators.LogLevel[] logLevels);

    @Query("SELECT sensors.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "(SELECT COUNT(`logs`.elementId) FROM `logs` WHERE `logs`.elementId=sensors.id " +
            "AND `logs`.elementType IN(:elementTypes) AND `logs`.logLevel IN(:logLevels) " +
            "AND `logs`.dateRegistered >= date('now','-5 minute')) " +
            "AS recentErrorLogs " +
            "FROM sensors " +
            "INNER JOIN networks ON sensors.networkId=networks.id " +
            "WHERE sensors.id=:id LIMIT 1")
    public abstract LiveData<SensorExtended> findByIdExtended(int id,
                                                              Enumerators.ElementType[] elementTypes,
                                                              Enumerators.LogLevel[] logLevels);

    @Insert
    public abstract void insert(Sensor sensor);

    @Insert
    public abstract void insertAll(Sensor... sensors);

    @Delete
    public abstract void delete(Sensor sensor);

    @Delete
    public abstract void deteleAll(Sensor... sensors);

    @Query("DELETE FROM networks WHERE id=:id")
    protected abstract void deleteById(Integer id);

    @Update
    public abstract void update(Sensor sensor);

    @Query("UPDATE `logs` SET elementName=:name WHERE id=:id AND elementType=:type")
    protected abstract void updateLogElementName(Integer id, Enumerators.ElementType type, String name);

    @Query("DELETE FROM `logs` WHERE id=:id AND elementType=:type")
    protected abstract void deleteLogsForElement(Integer id, Enumerators.ElementType type);

    @Transaction
    public void updateExtended(Sensor sensor) {
        update(sensor);
        updateLogElementName(sensor.getId(), Enumerators.ElementType.SENSOR, sensor.getName());
    }

    @Transaction
    public void deleteExtended(Sensor sensor) {
        delete(sensor);
        deleteLogsForElement(sensor.getId(), Enumerators.ElementType.SENSOR);
    }

    @Transaction
    public void deleteExtended(Integer id) {
        deleteById(id);
        deleteLogsForElement(id, Enumerators.ElementType.SENSOR);
    }

}
