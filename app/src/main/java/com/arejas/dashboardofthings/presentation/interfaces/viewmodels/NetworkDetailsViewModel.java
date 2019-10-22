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
import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.NetworkManagementUseCase;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;

import java.util.List;

public class NetworkDetailsViewModel extends AndroidViewModel {

    private final NetworkManagementUseCase networkManagementUseCase;
    private final LogsManagementUseCase logsManagementUseCase;

    private Integer networkId;

    private LiveData<Resource<NetworkExtended>> network;
    private LiveData<Resource<List<Sensor>>> sensorsRelated;
    private LiveData<Resource<List<Actuator>>> actuatorsRelated;
    private LiveData<Resource<List<Log>>> logs;

    public NetworkDetailsViewModel(@NonNull Application application, Integer networkIdToLoad,
                                   NetworkManagementUseCase networkManagementUseCase,
                                   LogsManagementUseCase logsManagementUseCase) {
        super(application);
        this.networkManagementUseCase = networkManagementUseCase;
        this.networkId = networkIdToLoad;
        this.logsManagementUseCase = logsManagementUseCase;
    }

    public LiveData<Resource<NetworkExtended>> getNetwork(boolean refreshData) {
        if (refreshData) network = null;
        if (networkId == null) return null;
        if (network == null) {
            network = this.networkManagementUseCase.getNetwork(networkId);
        }
        return network;
    }

    public LiveData<Resource<List<Sensor>>> getSensorsRelated(boolean refreshData) {
        if (refreshData) sensorsRelated = null;
        if (networkId == null) return null;
        if (sensorsRelated == null) {
            sensorsRelated = this.networkManagementUseCase.getListOfRelatedSensors(networkId);
        }
        return sensorsRelated;
    }

    public LiveData<Resource<List<Actuator>>> getActuatorsRelated(boolean refreshData) {
        if (refreshData) actuatorsRelated = null;
        if (networkId == null) return null;
        if (actuatorsRelated == null) {
            actuatorsRelated = this.networkManagementUseCase.getListOfRelatedActuators(networkId);
        }
        return actuatorsRelated;
    }

    public LiveData<Resource<List<Log>>> getLogsForNetwork(boolean refreshData) {
        if (refreshData) logs = null;
        if (networkId == null) return null;
        if (logs == null) {
            logs = this.logsManagementUseCase.getLastLogsForNetwork(networkId);
        }
        return logs;
    }

    public void createNetwork(Network network) {
        final LiveData<Resource>
                resultLiveData = this.networkManagementUseCase.createNetwork(network);
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

    public void updateNetwork(Network network) {
        final LiveData<Resource>
                resultLiveData = this.networkManagementUseCase.updateNetwork(network);
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

    public void removeNetwork(Network network) {
        final LiveData<Resource>
                resultLiveData = this.networkManagementUseCase.deleteNetwork(network);
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
