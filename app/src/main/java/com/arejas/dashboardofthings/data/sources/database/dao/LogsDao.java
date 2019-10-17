package com.arejas.dashboardofthings.data.sources.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.List;

@Dao
public abstract class LogsDao {

    @Query("SELECT * FROM `logs`")
    public abstract LiveData<List<Log>> getAll();

    @Query("SELECT * FROM `logs` ORDER BY dateRegistered DESC LIMIT 100")
    public abstract LiveData<List<Log>> getAllLastHundredLogs();

    @Query("SELECT * FROM `logs` WHERE logLevel IN(:logLevels) ORDER BY dateRegistered DESC LIMIT 100")
    public abstract LiveData<List<Log>> getAllLastHundredLogs(Enumerators.LogLevel[] logLevels);

    @Query("SELECT * FROM `logs` WHERE elementId=:id AND elementType=:elementType LIMIT 1")
    public abstract LiveData<Log> findLastForElementId(int id, Enumerators.ElementType elementType);

    @Query("SELECT * FROM `logs` WHERE elementId=:id AND elementType=:elementType " +
            "AND logLevel IN(:logLevels) LIMIT 1")
    public abstract LiveData<Log> findLastForElementId(int id, Enumerators.ElementType elementType,
                                                       Enumerators.LogLevel[] logLevels);

    @Query("SELECT * FROM `logs` WHERE elementId=:id AND elementType=:elementType ORDER BY dateRegistered DESC LIMIT 100")
    public abstract LiveData<List<Log>> getLastHundredLogsForElementId(int id, Enumerators.ElementType elementType);

    @Query("SELECT * FROM `logs` WHERE dateRegistered IN (SELECT MAX(dateRegistered) FROM `logs` GROUP BY ElementId, elementType)")
    public abstract LiveData<List<Log>> getLastLogForEachElement();

    @Query("SELECT * FROM `logs` WHERE dateRegistered IN (SELECT MAX(dateRegistered) FROM `logs` WHERE elementType IN(:elementTypes) " +
            "GROUP BY ElementId, elementType)")
    public abstract LiveData<List<Log>> getLastLogForEachElement(Enumerators.ElementType[] elementTypes);

    @Query("SELECT * FROM `logs` WHERE dateRegistered IN (SELECT MAX(dateRegistered) FROM `logs` " +
            "WHERE logLevel IN(:logLevels) GROUP BY ElementId, elementType)")
    public abstract LiveData<List<Log>> getLastLogForEachElement(Enumerators.LogLevel[] logLevels);

    @Query("SELECT * FROM `logs` WHERE dateRegistered IN (SELECT MAX(dateRegistered) FROM `logs` " +
            "WHERE elementType IN(:elementTypes) AND logLevel IN(:logLevels) " +
            "GROUP BY ElementId, elementType)")
    public abstract LiveData<List<Log>> getLastLogForEachElement(Enumerators.ElementType[] elementTypes,
                                                                 Enumerators.LogLevel[] logLevels);

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

    @Query("UPDATE `logs` SET elementName=:name WHERE id=:id AND elementType=:type")
    public abstract void updateElementName(Integer id, Enumerators.ElementType type, String name);

    @Delete
    protected abstract void delete(Log log);

    @Delete
    protected abstract void deteleAll(Log... logs);

    @Query("DELETE FROM `logs` WHERE id=:id AND elementType=:type")
    public abstract void deleteAll(Integer id, Enumerators.ElementType type);

    @Query("DELETE FROM `logs` where id NOT IN (SELECT id from `logs` ORDER BY dateRegistered DESC LIMIT 10000)")
    protected abstract void deleteOldValues();

}
