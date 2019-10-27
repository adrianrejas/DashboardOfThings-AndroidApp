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
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;

import java.util.List;

public class ActuatorListViewModel extends AndroidViewModel {

    private final ActuatorManagementUseCase actuatorManagementUseCase;

    private LiveData<Resource<List<ActuatorExtended>>> actuators;

    public ActuatorListViewModel(@NonNull Application application,
                                 ActuatorManagementUseCase actuatorManagementUseCase) {
        super(application);
        this.actuatorManagementUseCase = actuatorManagementUseCase;
    }

    public LiveData<Resource<List<ActuatorExtended>>> getListOfActuators(boolean refreshData) {
        if (refreshData) actuators = null;
        if (actuators == null) {
            actuators = this.actuatorManagementUseCase.getListOfActuators();
        }
        return actuators;
    }

    public void removeActuator(Actuator actuator) {
        final LiveData<Resource>
                resultLiveData = this.actuatorManagementUseCase.deleteActuator(actuator);
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
