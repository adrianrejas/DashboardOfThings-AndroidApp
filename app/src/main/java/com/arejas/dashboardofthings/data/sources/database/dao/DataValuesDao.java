package com.arejas.dashboardofthings.data.sources.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.arejas.dashboardofthings.domain.entities.DataValue;

import java.util.List;

@Dao
public abstract class DataValuesDao {

    @Query("SELECT * FROM `values`")
    public abstract LiveData<List<DataValue>> getAll();

    @Query("SELECT * FROM `values` WHERE sensorId=:id LIMIT 1")
    public abstract LiveData<DataValue> findLastForSensorId(int id);

    @Query("SELECT * FROM `values` WHERE sensorId IN(:ids)")
    public abstract LiveData<DataValue> findLastForSensorIds(int[] ids);

    @Query("SELECT * FROM `values` WHERE sensorId=:id ORDER BY dateReceived DESC LIMIT 20")
    public abstract LiveData<List<DataValue>> getLastValuesForSensorId(int id);

    @Query("SELECT `values`.id, `values`.sensorId, " +
            "cast(avg(cast(`values`.value AS FLOAT)) AS STRING) as value, " +
            "max(`values`.dateReceived) as dateReceived " +
            "FROM `values` " +
            "WHERE sensorId=:id AND dateReceived >= date('now','-1 day') " +
            "GROUP BY strftime('%H', `values`.dateReceived)")
    public abstract LiveData<List<DataValue>> getAvgLastOneDayValuesForSensorId(int id);

    @Query("SELECT * FROM `values` WHERE sensorId=:id AND dateReceived >= date('now','-1 day')")
    public abstract LiveData<List<DataValue>> getAllLastOneDayValuesForSensorId(int id);

    @Query("SELECT `values`.id, `values`.sensorId, " +
            "cast(avg(cast(`values`.value AS FLOAT)) AS STRING) as value, " +
            "max(`values`.dateReceived) as dateReceived " +
            "FROM `values` " +
            "WHERE sensorId=:id AND dateReceived >= date('now','-1 week') " +
            "GROUP BY strftime('%w', `values`.dateReceived)")
    public abstract LiveData<List<DataValue>> getAvgLastOneWeekValuesForSensorId(int id);

    @Query("SELECT * FROM `values` WHERE sensorId=:id AND dateReceived >= date('now','-1 week')")
    public abstract LiveData<List<DataValue>> getAllLastOneWeekValuesForSensorId(int id);

    @Query("SELECT `values`.id, `values`.sensorId, " +
            "cast(avg(cast(`values`.value AS FLOAT)) AS STRING) as value, " +
            "max(`values`.dateReceived) as dateReceived " +
            "FROM `values` " +
            "WHERE sensorId=:id AND dateReceived >= date('now','-1 month') " +
            "GROUP BY strftime('%d', `values`.dateReceived)")
    public abstract LiveData<List<DataValue>> getAvgLastOneMonthValuesForSensorId(int id);

    @Query("SELECT * FROM `values` WHERE sensorId=:id AND dateReceived >= date('now','-1 month')")
    public abstract LiveData<List<DataValue>> getAllLastOneMonthValuesForSensorId(int id);

    @Query("SELECT `values`.id, `values`.sensorId, " +
            "cast(avg(cast(`values`.value AS FLOAT)) AS STRING) as value, " +
            "max(`values`.dateReceived) as dateReceived " +
            "FROM `values` " +
            "WHERE sensorId=:id AND dateReceived >= date('now','-1 year') " +
            "GROUP BY strftime('%m', `values`.dateReceived)")
    public abstract LiveData<List<DataValue>> getAvgLastOneYearValuesForSensorId(int id);

    @Query("SELECT * FROM `values` WHERE sensorId=:id AND dateReceived >= date('now','-1 year')")
    public abstract LiveData<List<DataValue>> getAllLastOneYearValuesForSensorId(int id);

    @Insert
    protected abstract void insertInner(DataValue value);

    @Insert
    protected abstract void insertAllInner(DataValue... values);

    @Transaction
    public void insert(DataValue value) {
        insertInner(value);
        deleteOldValues();
    }

    @Transaction
    public void insertAll(DataValue... values) {
        insertAllInner(values);
        deleteOldValues();
    }

    @Delete
    protected abstract void delete(DataValue value);

    @Delete
    protected abstract void deteleAll(DataValue... values);

    @Query("DELETE FROM `values` WHERE dateReceived <= date('now','-1 year')")
    protected abstract void deleteOldValues();
}
