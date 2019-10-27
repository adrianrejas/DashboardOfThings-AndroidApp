package com.arejas.dashboardofthings.presentation.interfaces.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;

import java.util.List;
import java.util.Map;

public class SensorDetailsViewModel extends AndroidViewModel {

    private final SensorManagementUseCase sensorManagementUseCase;
    private final LogsManagementUseCase logsManagementUseCase;
    private final DataManagementUseCase dataManagementUseCase;

    private int historySpinnerPosition;

    private Integer sensorId;

    private LiveData<Resource<SensorExtended>> sensor;
    private LiveData<Resource<List<Log>>> logs;
    private LiveData<Resource<DataValue>> lastValue;

    private LiveData<Resource<List<DataValue>>> sensorLastValuesLiveData;
    private LiveData<Resource<List<DataValue>>> sensorLastHourLiveData;
    private LiveData<Resource<List<DataValue>>> sensorLastWeekLiveData;
    private LiveData<Resource<List<DataValue>>> sensorLastMonthLiveData;
    private LiveData<Resource<List<DataValue>>> sensorLastYearLiveData;

    public SensorDetailsViewModel(@NonNull Application application,
                                  SensorManagementUseCase sensorManagementUseCase,
                                  DataManagementUseCase dataManagementUseCase,
                                  LogsManagementUseCase logsManagementUseCase) {
        super(application);
        this.sensorManagementUseCase = sensorManagementUseCase;
        this.logsManagementUseCase = logsManagementUseCase;
        this.dataManagementUseCase = dataManagementUseCase;
        this.historySpinnerPosition = 0;
        this.sensorId = null;
    }

    public Integer getSensorId() {
        return sensorId;
    }

    public Integer setSensorId(Integer id) {
        if (this.sensorId != id) {
            this.sensorId = id;
            sensor = null;
            logs = null;
            lastValue = null;
            sensorLastValuesLiveData = null;
            sensorLastHourLiveData = null;
            sensorLastWeekLiveData = null;
            sensorLastMonthLiveData = null;
            sensorLastYearLiveData = null;
            historySpinnerPosition = 0;
        }
        return this.sensorId;
    }

    public int getHistorySpinnerPosition() {
        return historySpinnerPosition;
    }

    public void setHistorySpinnerPosition(int historySpinnerPosition) {
        this.historySpinnerPosition = historySpinnerPosition;
    }

    public LiveData<Resource<SensorExtended>> getSensor(boolean refreshData) {
        if (refreshData) sensor = null;
        if (sensorId == null) return null;
        if (sensor == null) {
            sensor = this.sensorManagementUseCase.getSensor(sensorId);
        }
        return sensor;
    }

    public LiveData<Resource<DataValue>> getLastValueForSensor(boolean refreshData) {
        if (refreshData) lastValue = null;
        if (sensorId == null) return null;
        if (lastValue == null) {
            lastValue = this.dataManagementUseCase.findLastForSensorId(sensorId);
        }
        return lastValue;
    }

    public LiveData<Resource<List<Log>>> getLogsForSensor(boolean refreshData) {
        if (refreshData) logs = null;
        if (sensorId == null) return null;
        if (logs == null) {
            logs = this.logsManagementUseCase.getLastLogsForSensor(sensorId);
        }
        return logs;
    }

    public LiveData<Resource> requestSensorReload() {
        if ((sensor != null) && (sensor.getValue() != null) &&
                (sensor.getValue().getStatus().equals(Resource.Status.SUCCESS)))
            return this.dataManagementUseCase.requestSensorReload(sensor.getValue().getData());
        return null;
    }

    public void removeSensor(Sensor sensor) {
        final LiveData<Resource>
                resultLiveData = this.sensorManagementUseCase.deleteSensor(sensor);
        Observer observer = new Observer<Resource>() {
            @Override
            public void onChanged(@Nullable Resource result) {
                if(result!= null) {
                    if (result.getStatus().equals(Resource.Status.LOADING)) {
                        ToastHelper.showToast(DotApplication.getContext().getString(R.string.toast_removing));
                    } else if (result.getStatus().equals(Resource.Status.SUCCESS)) {
                        ToastHelper.showToast(DotApplication.getContext().getString(R.string.toast_remove_succesful));
                        resultLiveData.removeObserver(this);
                    } else if (result.getStatus().equals(Resource.Status.ERROR)) {
                        ToastHelper.showToast(DotApplication.getContext().getString(R.string.toast_remove_failed));
                        resultLiveData.removeObserver(this);
                    }
                    return;
                }
            }
        };
        resultLiveData.observeForever(observer);
    }

    public LiveData<Resource<List<DataValue>>> getHistoricalData(boolean refreshData) {
        switch (historySpinnerPosition) {
            case 1:
                return getAvgLastOneDayValuesForSensor(refreshData);
            case 2:
                return getAvgLastOneWeekValuesForSensor(refreshData);
            case 3:
                return getAvgLastOneMonthValuesForSensor(refreshData);
            case 4:
                return getAvgLastOneYearValuesForSensor(refreshData);
            default:
                return getLastValuesForSensor(refreshData);
        }
    }

    public LiveData<Resource<List<DataValue>>> getLastValuesForSensor(boolean refreshData) {
        if (refreshData) sensorLastValuesLiveData = null;
        if (sensorId == null) return null;
        if ((sensorLastValuesLiveData == null)) {
            sensorLastValuesLiveData = this.dataManagementUseCase.getLastValuesForSensorId(sensorId);
        }
        return sensorLastValuesLiveData;
    }

    public LiveData<Resource<List<DataValue>>> getAvgLastOneDayValuesForSensor(boolean refreshData) {
        if (refreshData) sensorLastHourLiveData = null;
        if (sensorId == null) return null;
        if ((sensorLastHourLiveData == null)) {
            sensorLastHourLiveData = this.dataManagementUseCase.getAvgLastOneDayValuesForSensorId(sensorId);
        }
        return sensorLastHourLiveData;
    }

    public LiveData<Resource<List<DataValue>>> getAvgLastOneWeekValuesForSensor(boolean refreshData) {
        if (refreshData) sensorLastWeekLiveData = null;
        if (sensorId == null) return null;
        if ((sensorLastWeekLiveData == null)) {
            sensorLastWeekLiveData = this.dataManagementUseCase.getAvgLastOneWeekValuesForSensorId(sensorId);
        }
        return sensorLastWeekLiveData;
    }

    public LiveData<Resource<List<DataValue>>> getAvgLastOneMonthValuesForSensor(boolean refreshData) {
        if (refreshData) sensorLastMonthLiveData = null;
        if (sensorId == null) return null;
        if ((sensorLastMonthLiveData == null)) {
            sensorLastMonthLiveData = this.dataManagementUseCase.getAvgLastOneMonthValuesForSensorId(sensorId);
        }
        return sensorLastMonthLiveData;
    }

    public LiveData<Resource<List<DataValue>>> getAvgLastOneYearValuesForSensor(boolean refreshData) {
        if (refreshData) sensorLastYearLiveData = null;
        if (sensorId == null) return null;
        if ((sensorLastYearLiveData == null)) {
            sensorLastYearLiveData = this.dataManagementUseCase.getAvgLastOneYearValuesForSensorId(sensorId);
        }
        return sensorLastYearLiveData;
    }

}
