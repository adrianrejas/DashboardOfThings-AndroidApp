package com.arejas.dashboardofthings.presentation.interfaces.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.usecases.NetworkManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;

import java.util.List;

public class SensorAddEditViewModel extends AndroidViewModel {

    private final NetworkManagementUseCase networkManagementUseCase;
    private final SensorManagementUseCase sensorManagementUseCase;

    private Integer sensorId;

    private SensorExtended sensorBeingEdited;

    private LiveData<Resource<SensorExtended>> sensor;
    private LiveData<Resource<List<NetworkExtended>>> networks;

    public SensorAddEditViewModel(@NonNull Application application,
                                  NetworkManagementUseCase networkManagementUseCase,
                                  SensorManagementUseCase sensorManagementUseCase) {
        super(application);
        this.networkManagementUseCase = networkManagementUseCase;
        this.sensorManagementUseCase = sensorManagementUseCase;
        this.sensorId = null;
    }

    public Integer getSensorId() {
        return sensorId;
    }

    public Integer setSensorId(Integer id) {
        if ((id == null) || (this.sensorId != id)) {
            sensor = null;
            sensorBeingEdited = null;
            sensor = null;
            this.sensorId = id;
            networks = null;
        }
        return this.sensorId;
    }

    public SensorExtended getSensorBeingEdited() {
        return sensorBeingEdited;
    }

    public void setSensorBeingEdited(SensorExtended sensorBeingEdited) {
        this.sensorBeingEdited = sensorBeingEdited;
    }

    public LiveData<Resource<SensorExtended>> getSensor(boolean refreshData) {
        if (refreshData) sensor = null;
        if (sensorId == null) return null;
        if (sensor == null) {
            sensor = this.sensorManagementUseCase.getSensor(sensorId);
        }
        return sensor;
    }

    public LiveData<Resource<List<NetworkExtended>>> getNetworks(boolean refreshData) {
        if (refreshData) networks = null;
        if (networks == null) {
            networks = this.networkManagementUseCase.getListOfNetworks();
        }
        return networks;
    }

    public void createSensor(Sensor sensor) {
        final LiveData<Resource>
                resultLiveData = this.sensorManagementUseCase.createSensor(sensor);
        Observer observer = new Observer<Resource>() {
            @Override
            public void onChanged(@Nullable Resource result) {
                if(result!= null) {
                    if (result.getStatus().equals(Resource.Status.LOADING)) {
                        ToastHelper.showToast(DotApplication.getContext().getString(R.string.toast_creating));
                    } else if (result.getStatus().equals(Resource.Status.SUCCESS)) {
                        ToastHelper.showToast(DotApplication.getContext().getString(R.string.toast_create_succesful));
                        resultLiveData.removeObserver(this);
                    } else if (result.getStatus().equals(Resource.Status.ERROR)) {
                        ToastHelper.showToast(DotApplication.getContext().getString(R.string.toast_create_failed));
                        resultLiveData.removeObserver(this);
                    }
                    return;
                }
            }
        };
        resultLiveData.observeForever(observer);
    }

    public void updateSensor(Sensor sensor) {
        final LiveData<Resource>
                resultLiveData = this.sensorManagementUseCase.updateSensor(sensor);
        Observer observer = new Observer<Resource>() {
            @Override
            public void onChanged(@Nullable Resource result) {
                if(result!= null) {
                    if (result.getStatus().equals(Resource.Status.LOADING)) {
                        ToastHelper.showToast(DotApplication.getContext().getString(R.string.toast_updating));
                    } else if (result.getStatus().equals(Resource.Status.SUCCESS)) {
                        ToastHelper.showToast(DotApplication.getContext().getString(R.string.toast_update_succesful));
                        resultLiveData.removeObserver(this);
                    } else if (result.getStatus().equals(Resource.Status.ERROR)) {
                        ToastHelper.showToast(DotApplication.getContext().getString(R.string.toast_update_failed));
                        resultLiveData.removeObserver(this);
                    }
                    return;
                }
            }
        };
        resultLiveData.observeForever(observer);
    }

}
