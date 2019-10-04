package com.arejas.dashboardofthings.data.sources.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.arejas.dashboardofthings.domain.entities.DataValue;
import com.arejas.dashboardofthings.domain.entities.Sensor;

import java.util.Date;
import java.util.List;

@Dao
public abstract class DataValuesDao {

    @Query("SELECT * FROM `values`")
    public abstract LiveData<List<DataValue>> getAll();

    @Query("SELECT * FROM `values` WHERE sensorId=:id LIMIT 1")
    public abstract LiveData<DataValue> findLastForSensorId(int id);

    @Query("SELECT * FROM `values` WHERE sensorId=:id AND dateReceived >= date('now','-1 day')")
    public abstract LiveData<List<DataValue>> getLastOneDayValuesForSensorId(int id);

    @Query("SELECT * FROM `values` WHERE sensorId=:id AND dateReceived >= date('now','-1 week')")
    public abstract LiveData<List<DataValue>> getLastOneWeekValuesForSensorId(int id);

    @Query("SELECT * FROM `values` WHERE sensorId=:id AND dateReceived >= date('now','-1 month')")
    public abstract LiveData<List<DataValue>> getLastOneMonthValuesForSensorId(int id);

    @Query("SELECT * FROM `values` WHERE sensorId=:id AND dateReceived >= date('now','-1 year')")
    public abstract LiveData<List<DataValue>> getLastOneYearValuesForSensorId(int id);

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
