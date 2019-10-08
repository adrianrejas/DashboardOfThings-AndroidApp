package com.arejas.dashboardofthings.data.sources.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.arejas.dashboardofthings.domain.entities.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;

import java.util.List;

@Dao
public abstract class SensorsDao {

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
            "`values`.value AS lastValue, `values`.dateReceived AS dateReceivedLastValue " +
            "FROM sensors " +
            "INNER JOIN networks ON sensors.networkId=networks.id " +
            "INNER JOIN `values` ON sensors.networkId=`values`.sensorId")
    public abstract LiveData<List<SensorExtended>> getAllExtended();

    @Query("SELECT sensors.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "`values`.value AS lastValue, `values`.dateReceived AS dateReceivedLastValue " +
            "FROM sensors " +
            "INNER JOIN networks ON sensors.networkId=networks.id " +
            "INNER JOIN `values` ON sensors.networkId=`values`.sensorId " +
            "WHERE networkId=:networkId")
    public abstract LiveData<List<SensorExtended>> getAllExtendedFromSameNetwork(int networkId);

    @Query("SELECT sensors.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "`values`.value AS lastValue, `values`.dateReceived AS dateReceivedLastValue " +
            "FROM sensors " +
            "INNER JOIN networks ON sensors.networkId=networks.id " +
            "INNER JOIN `values` ON sensors.networkId=`values`.sensorId " +
            "WHERE showInMainDashboard=1")
    public abstract LiveData<List<SensorExtended>> getAllExtendedToBeShownInMainDashboard();

    @Query("SELECT sensors.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "`values`.value AS lastValue, `values`.dateReceived AS dateReceivedLastValue " +
            "FROM sensors " +
            "INNER JOIN networks ON sensors.networkId=networks.id " +
            "INNER JOIN `values` ON sensors.networkId=`values`.sensorId " +
            "WHERE locationLat IS NOT NULL AND localtionLong IS NOT NULL")
    public abstract LiveData<List<SensorExtended>> getAllExtendedLocated();

    @Query("SELECT sensors.*, networks.name AS networkName, networks.networkType AS networkType, " +
            "`values`.value AS lastValue, `values`.dateReceived AS dateReceivedLastValue " +
            "FROM sensors " +
            "INNER JOIN networks ON sensors.networkId=networks.id " +
            "INNER JOIN `values` ON sensors.networkId=`values`.sensorId " +
            "WHERE sensors.id=:id LIMIT 1")
    public abstract LiveData<SensorExtended> findByIdExtended(int id);

    @Insert
    public abstract void insert(Sensor sensor);

    @Insert
    public abstract void insertAll(Sensor... sensors);

    @Delete
    public abstract void delete(Sensor sensor);

    @Delete
    public abstract void deteleAll(Sensor... sensors);

    @Update
    public abstract void update(Sensor sensor);

}
