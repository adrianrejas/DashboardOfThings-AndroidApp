package com.arejas.dashboardofthings.data.sources.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.arejas.dashboardofthings.domain.entities.database.DataValue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Dao
public abstract class DataValuesDao {

    @Query("SELECT * FROM `values`")
    public abstract LiveData<List<DataValue>> getAll();

    @Query("SELECT `values`.id, `values`.sensorId, `values`.value, " +
            "MAX(`values`.dateReceived) AS dateReceived FROM `values` GROUP BY `values`.sensorId")
    public abstract LiveData<List<DataValue>> getLastValuesForAll();

    @Query("SELECT `values`.id, `values`.sensorId, `values`.value, " +
            "MAX(`values`.dateReceived) AS dateReceived FROM `values` " +
            "INNER JOIN sensors ON sensors.id=`values`.sensorId " +
            "WHERE sensors.showInMainDashboard " +
            "GROUP BY `values`.sensorId")
    public abstract LiveData<List<DataValue>> getLastValuesForAllInMainDashboard();

    @Query("SELECT * FROM `values` WHERE sensorId=:id ORDER BY dateReceived DESC LIMIT 1")
    public abstract LiveData<DataValue> findLastForSensorId(int id);

    @Query("SELECT * FROM `values` WHERE sensorId=:id ORDER BY dateReceived DESC LIMIT 1")
    public abstract DataValue findLastForSensorIdInstant(int id);

    @Query("SELECT `values`.id, `values`.sensorId, `values`.value, " +
            "MAX(`values`.dateReceived) AS dateReceived FROM `values` WHERE sensorId IN(:ids) GROUP BY `values`.sensorId")
    public abstract LiveData<List<DataValue>> findLastForSensorIds(int[] ids);

    @Query("SELECT * FROM `values` WHERE sensorId=:id ORDER BY dateReceived DESC LIMIT 20")
    public abstract LiveData<List<DataValue>> getLastValuesForSensorId(int id);

    @Query("SELECT `values`.id, `values`.sensorId, " +
            "cast(avg(cast(`values`.value AS FLOAT)) AS STRING) as value, " +
            "max(`values`.dateReceived) as dateReceived " +
            "FROM `values` " +
            "WHERE sensorId=:id AND dateReceived >= :date " +
            "GROUP BY strftime('%H', datetime(`values`.dateReceived/1000, 'unixepoch'))")
    public abstract LiveData<List<DataValue>> getAvgHourValuesForSensorId(int id, Date date);

    @Query("SELECT `values`.id, `values`.sensorId, " +
            "cast(avg(cast(`values`.value AS FLOAT)) AS STRING) as value, " +
            "max(`values`.dateReceived) as dateReceived " +
            "FROM `values` " +
            "WHERE sensorId=:id AND dateReceived >= :date " +
            "GROUP BY strftime('%w', datetime(`values`.dateReceived/1000, 'unixepoch'))")
    public abstract LiveData<List<DataValue>> getAvgWeekdayValuesForSensorId(int id, Date date);

    @Query("SELECT `values`.id, `values`.sensorId, " +
            "cast(avg(cast(`values`.value AS FLOAT)) AS STRING) as value, " +
            "max(`values`.dateReceived) as dateReceived " +
            "FROM `values` " +
            "WHERE sensorId=:id AND dateReceived >= :date " +
            "GROUP BY strftime('%d', datetime(`values`.dateReceived/1000, 'unixepoch'))")
    public abstract LiveData<List<DataValue>> getAvgPerMonthDayValuesForSensorId(int id, Date date);

    @Query("SELECT `values`.id, `values`.sensorId, " +
            "cast(avg(cast(`values`.value AS FLOAT)) AS STRING) as value, " +
            "max(`values`.dateReceived) as dateReceived " +
            "FROM `values` " +
            "WHERE sensorId=:id AND dateReceived >= :date " +
            "GROUP BY strftime('%m', datetime(`values`.dateReceived/1000, 'unixepoch'))")
    public abstract LiveData<List<DataValue>> getAvgPerMonthValuesForSensorId(int id, Date date);

    @Query("SELECT * FROM `values` WHERE sensorId=:id AND dateReceived >= date('now','-1 year')")
    public abstract LiveData<List<DataValue>> getAllLastOneYearValuesForSensorId(int id);

    @Insert
    protected abstract void insertInner(DataValue value);

    @Insert
    protected abstract void insertAllInner(DataValue... values);

    @Transaction
    public void insert(DataValue value) {
        insertInner(value);
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.add(Calendar.YEAR, -1);
        Date prevYear = cal.getTime();
        deleteOldValues(prevYear);
    }

    @Transaction
    public void insertAll(DataValue... values) {
        insertAllInner(values);
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        cal.add(Calendar.YEAR, -1);
        Date prevYear = cal.getTime();
        deleteOldValues(prevYear);
    }

    @Delete
    protected abstract void delete(DataValue value);

    @Delete
    protected abstract void deteleAll(DataValue... values);

    @Query("DELETE FROM `values` WHERE dateReceived <= :date")
    protected abstract void deleteOldValues(Date date);
}
