package com.arejas.dashboardofthings.data.sources.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.arejas.dashboardofthings.domain.entities.DataValue;
import com.arejas.dashboardofthings.domain.entities.Log;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.List;

@Dao
public abstract class LogsDao {

    @Query("SELECT * FROM `logs`")
    public abstract LiveData<List<Log>> getAll();

    @Query("SELECT * FROM `logs` WHERE elementId=:id AND elementType=:elementType LIMIT 1")
    public abstract LiveData<Log> findLastForSensorId(int id, Enumerators.ElementType elementType);

    @Query("SELECT * FROM `logs` WHERE elementId=:id AND elementType=:elementType ORDER BY dateRegistered DESC LIMIT 100")
    public abstract LiveData<List<Log>> getLastHundredLogsForSensorId(int id, Enumerators.ElementType elementType);

    @Insert
    protected abstract void insertInner(Log log);

    @Insert
    protected abstract void insertAllInner(Log... logs);


    @Transaction
    public void insert(Log log) {
        insertInner(log);
        deleteOldValues();
    }

    @Transaction
    public void insertAll(Log... logs) {
        insertAllInner(logs);
        deleteOldValues();
    }
    @Delete
    protected abstract void delete(Log log);

    @Delete
    protected abstract void deteleAll(Log... logs);

    @Query("DELETE FROM `logs` where id NOT IN (SELECT id from `logs` ORDER BY dateRegistered DESC LIMIT 10000)")
    protected abstract void deleteOldValues();

}
