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

public class NetworkAddEditViewModel extends AndroidViewModel {

    private final NetworkManagementUseCase networkManagementUseCase;

    private Integer networkId;

    private Network networkBeingEdited;

    private LiveData<Resource<NetworkExtended>> network;

    public NetworkAddEditViewModel(@NonNull Application application, NetworkManagementUseCase networkManagementUseCase) {
        super(application);
        this.networkManagementUseCase = networkManagementUseCase;
        this.networkId = null;
    }

    public Integer getNetworkId() {
        return networkId;
    }

    public Integer setNetworkId(Integer id) {
        if ((id == null) || (this.networkId != id)) {
            network = null;
            networkBeingEdited = null;
            network = null;
            this.networkId = id;
        }
        return this.networkId;
    }

    public Network getNetworkBeingEdited() {
        return networkBeingEdited;
    }

    public void setNetworkBeingEdited(Network networkBeingEdited) {
        this.networkBeingEdited = networkBeingEdited;
    }

    public LiveData<Resource<NetworkExtended>> getNetwork(boolean refreshData) {
        if (refreshData) network = null;
        if (networkId == null) return null;
        if (network == null) {
            network = this.networkManagementUseCase.getNetwork(networkId);
        }
        return network;
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

}
