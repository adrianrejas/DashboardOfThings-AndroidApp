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
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;

import java.util.List;

public class SensorListViewModel extends AndroidViewModel {

    private final SensorManagementUseCase sensorManagementUseCase;

    private LiveData<Resource<List<SensorExtended>>> sensors;

    public SensorListViewModel(@NonNull Application application,
                               SensorManagementUseCase sensorManagementUseCase) {
        super(application);
        this.sensorManagementUseCase = sensorManagementUseCase;
    }

    public LiveData<Resource<List<SensorExtended>>> getListOfSensors(boolean refreshData) {
        if (refreshData) sensors = null;
        if (sensors == null) {
            sensors = this.sensorManagementUseCase.getListOfSensors();
        }
        return sensors;
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

}
