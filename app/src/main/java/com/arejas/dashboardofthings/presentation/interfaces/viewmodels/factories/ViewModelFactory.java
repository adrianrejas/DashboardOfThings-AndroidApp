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
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkAddEditViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkListViewModel;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkAddEditActivity;

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
    private NetworkListViewModel networkListViewModel;
    private NetworkDetailsViewModel networkDetailsViewModel;
    private NetworkAddEditViewModel networkAddEditViewModel;

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
        networkListViewModelSingleton();
        this.networkDetailsViewModel = null;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        if (modelClass.isAssignableFrom(MainDashboardViewModel.class)) {
            return (T) mainDashboardViewModelSingleton();
        } else if (modelClass.isAssignableFrom(NetworkListViewModel.class)) {
            return (T) networkListViewModelSingleton();
        } else if (modelClass.isAssignableFrom(NetworkDetailsViewModel.class)) {
            return (T) networkDetailsViewModelSingleton();
        } else if (modelClass.isAssignableFrom(NetworkAddEditViewModel.class)) {
            return (T) networkAddEditViewModelSingleton();
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

    private NetworkListViewModel networkListViewModelSingleton() {
        if (networkListViewModel == null)
            networkListViewModel = new NetworkListViewModel(application,
                    networkManagementUseCase);
        return networkListViewModel;
    }

    private NetworkDetailsViewModel networkDetailsViewModelSingleton() {
        if (networkDetailsViewModel == null)
            networkDetailsViewModel = new NetworkDetailsViewModel(application, networkManagementUseCase, logsManagementUseCase);
        return networkDetailsViewModel;
    }

    private NetworkAddEditViewModel networkAddEditViewModelSingleton() {
        if (networkAddEditViewModel == null)
            networkAddEditViewModel = new NetworkAddEditViewModel(application, networkManagementUseCase);
        return networkAddEditViewModel;
    }

}
