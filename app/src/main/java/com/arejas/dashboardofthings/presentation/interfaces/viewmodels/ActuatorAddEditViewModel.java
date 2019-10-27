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
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.usecases.NetworkManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;

import java.util.List;

public class ActuatorAddEditViewModel extends AndroidViewModel {

    private final NetworkManagementUseCase networkManagementUseCase;
    private final ActuatorManagementUseCase actuatorManagementUseCase;

    private Integer actuatorId;

    private ActuatorExtended actuatorBeingEdited;

    private LiveData<Resource<ActuatorExtended>> actuator;
    private LiveData<Resource<List<NetworkExtended>>> networks;

    public ActuatorAddEditViewModel(@NonNull Application application,
                                    NetworkManagementUseCase networkManagementUseCase,
                                    ActuatorManagementUseCase actuatorManagementUseCase) {
        super(application);
        this.networkManagementUseCase = networkManagementUseCase;
        this.actuatorManagementUseCase = actuatorManagementUseCase;
        this.actuatorId = null;
    }

    public Integer getActuatorId() {
        return actuatorId;
    }

    public Integer setActuatorId(Integer id) {
        if ((id == null) || (this.actuatorId != id)) {
            actuator = null;
            actuatorBeingEdited = null;
            actuator = null;
            this.actuatorId = id;
            networks = null;
        }
        return this.actuatorId;
    }

    public ActuatorExtended getActuatorBeingEdited() {
        return actuatorBeingEdited;
    }

    public void setActuatorBeingEdited(ActuatorExtended actuatorBeingEdited) {
        this.actuatorBeingEdited = actuatorBeingEdited;
    }

    public LiveData<Resource<ActuatorExtended>> getActuator(boolean refreshData) {
        if (refreshData) actuator = null;
        if (actuatorId == null) return null;
        if (actuator == null) {
            actuator = this.actuatorManagementUseCase.getActuator(actuatorId);
        }
        return actuator;
    }

    public LiveData<Resource<List<NetworkExtended>>> getNetworks(boolean refreshData) {
        if (refreshData) networks = null;
        if (networks == null) {
            networks = this.networkManagementUseCase.getListOfNetworks();
        }
        return networks;
    }

    public void createActuator(Actuator actuator) {
        final LiveData<Resource>
                resultLiveData = this.actuatorManagementUseCase.createActuator(actuator);
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

    public void updateActuator(Actuator actuator) {
        final LiveData<Resource>
                resultLiveData = this.actuatorManagementUseCase.updateActuator(actuator);
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
