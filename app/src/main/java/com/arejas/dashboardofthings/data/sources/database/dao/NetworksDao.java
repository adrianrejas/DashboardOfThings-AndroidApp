package com.arejas.dashboardofthings.data.sources.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.arejas.dashboardofthings.domain.entities.Network;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkBasic;

import java.util.List;

@Dao
public abstract class NetworksDao {

    @Query("SELECT * FROM networks")
    public abstract LiveData<List<Network>> getAll();

    @Query("SELECT id, name FROM networks")
    public abstract LiveData<List<NetworkBasic>> getAllBasic();

    @Query("SELECT * FROM networks WHERE id=:id LIMIT 1")
    public abstract LiveData<Network> findById(int id);

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

}
