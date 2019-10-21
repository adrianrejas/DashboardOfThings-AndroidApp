package com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.NetworkManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MainDashboardViewModel;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final Application application;

    private final NetworkManagementUseCase networkManagementUseCase;
    private final SensorManagementUseCase sensorManagementUseCase;
    private final ActuatorManagementUseCase actuatorManagementUseCase;
    private final DataManagementUseCase dataManagementUseCase;
    private final LogsManagementUseCase logsManagementUseCase;

    private MainDashboardViewModel mainDashboardViewModel;

    @Inject
    public ViewModelFactory(@NonNull Application application,
                            NetworkManagementUseCase networkManagementUseCase,
                            SensorManagementUseCase sensorManagementUseCase,
                            ActuatorManagementUseCase actuatorManagementUseCase,
                            DataManagementUseCase dataManagementUseCase,
                            LogsManagementUseCase logsManagementUseCase) {
        this.application = application;
        this.networkManagementUseCase = networkManagementUseCase;
        this.sensorManagementUseCase = sensorManagementUseCase;
        this.actuatorManagementUseCase = actuatorManagementUseCase;
        this.dataManagementUseCase = dataManagementUseCase;
        this.logsManagementUseCase = logsManagementUseCase;
        mainDashboardViewModelSingleton();
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        if (modelClass.isAssignableFrom(MainDashboardViewModel.class)) {
            return (T) mainDashboardViewModelSingleton();
        } else {
            throw new ClassCastException("No view model class recognized");
        }
    }

    private MainDashboardViewModel mainDashboardViewModelSingleton() {
        if (mainDashboardViewModel == null)
            mainDashboardViewModel = new MainDashboardViewModel(application,
                    sensorManagementUseCase, actuatorManagementUseCase,
                    dataManagementUseCase, logsManagementUseCase);
        return mainDashboardViewModel;
    }

}
