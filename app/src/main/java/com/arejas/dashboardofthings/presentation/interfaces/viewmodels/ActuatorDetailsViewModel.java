package com.arejas.dashboardofthings.presentation.interfaces.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;

import java.util.List;

public class ActuatorDetailsViewModel extends AndroidViewModel {

    private final ActuatorManagementUseCase actuatorManagementUseCase;
    private final LogsManagementUseCase logsManagementUseCase;
    private final DataManagementUseCase dataManagementUseCase;

    private int historySpinnerPosition;

    private Integer actuatorId;

    private LiveData<Resource<ActuatorExtended>> actuator;
    private LiveData<Resource<List<Log>>> logs;

    public ActuatorDetailsViewModel(@NonNull Application application,
                                    ActuatorManagementUseCase actuatorManagementUseCase,
                                    DataManagementUseCase dataManagementUseCase,
                                    LogsManagementUseCase logsManagementUseCase) {
        super(application);
        this.actuatorManagementUseCase = actuatorManagementUseCase;
        this.logsManagementUseCase = logsManagementUseCase;
        this.dataManagementUseCase = dataManagementUseCase;
        this.historySpinnerPosition = 0;
        this.actuatorId = null;
    }

    public Integer getActuatorId() {
        return actuatorId;
    }

    public Integer setActuatorId(Integer id) {
        if (this.actuatorId != id) {
            this.actuatorId = id;
            actuator = null;
            logs = null;
            historySpinnerPosition = 0;
        }
        return this.actuatorId;
    }

    public int getHistorySpinnerPosition() {
        return historySpinnerPosition;
    }

    public void setHistorySpinnerPosition(int historySpinnerPosition) {
        this.historySpinnerPosition = historySpinnerPosition;
    }

    public LiveData<Resource<ActuatorExtended>> getActuator(boolean refreshData) {
        if (refreshData) actuator = null;
        if (actuatorId == null) return null;
        if (actuator == null) {
            actuator = this.actuatorManagementUseCase.getActuator(actuatorId);
        }
        return actuator;
    }

    public LiveData<Resource<List<Log>>> getLogsForActuator(boolean refreshData) {
        if (refreshData) logs = null;
        if (actuatorId == null) return null;
        if (logs == null) {
            logs = this.logsManagementUseCase.getLastLogsForActuator(actuatorId);
        }
        return logs;
    }

    public void sendActuatorData(Actuator actuator, String data) {
        final LiveData<Resource>
                resultLiveData = this.dataManagementUseCase.updateActuatorData(actuator, data);
        Observer observer = new Observer<Resource>() {
            @Override
            public void onChanged(@Nullable Resource result) {
                if(result!= null) {
                    if (result.getStatus().equals(Resource.Status.LOADING)) {
                        ToastHelper.showToast(DotApplication.getContext().getString(R.string.toast_actuator_send_loading));
                    } else if (result.getStatus().equals(Resource.Status.SUCCESS)) {
                        ToastHelper.showToast(DotApplication.getContext().getString(R.string.toast_actuator_send_success));
                        resultLiveData.removeObserver(this);
                    } else if (result.getStatus().equals(Resource.Status.ERROR)) {
                        ToastHelper.showToast(DotApplication.getContext().getString(R.string.toast_actuator_send_failed));
                        resultLiveData.removeObserver(this);
                    }
                    return;
                }
            }
        };
        resultLiveData.observeForever(observer);
    }

    public void sendActuatorData (String data) {
        if ((actuator != null) && (actuator.getValue() != null) &&
                (actuator.getValue().getStatus().equals(Resource.Status.SUCCESS)))
            sendActuatorData(actuator.getValue().getData(), data);
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
