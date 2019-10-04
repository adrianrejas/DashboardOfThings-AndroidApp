package com.arejas.dashboardofthings.data.sources.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.arejas.dashboardofthings.domain.entities.Actuator;

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

}
