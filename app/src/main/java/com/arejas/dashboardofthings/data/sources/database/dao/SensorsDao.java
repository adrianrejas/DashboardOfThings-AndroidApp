package com.arejas.dashboardofthings.data.sources.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.arejas.dashboardofthings.domain.entities.Sensor;

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
