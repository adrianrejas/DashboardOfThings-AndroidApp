package com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.NetworkManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorAddEditViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorListViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MainDashboardViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MapViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkAddEditViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkListViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorAddEditViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorListViewModel
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkAddEditActivity

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ViewModelFactory @Inject
constructor(
    private val application: Application,
    private val networkManagementUseCase: NetworkManagementUseCase,
    private val sensorManagementUseCase: SensorManagementUseCase,
    private val actuatorManagementUseCase: ActuatorManagementUseCase,
    private val dataManagementUseCase: DataManagementUseCase,
    private val logsManagementUseCase: LogsManagementUseCase
) : ViewModelProvider.NewInstanceFactory() {

    private lateinit var mainDashboardViewModel: MainDashboardViewModel
    private lateinit var networkListViewModel: NetworkListViewModel
    private lateinit var networkDetailsViewModel: NetworkDetailsViewModel
    private lateinit var networkAddEditViewModel: NetworkAddEditViewModel
    private lateinit var sensorListViewModel: SensorListViewModel
    private lateinit var sensorDetailsViewModel: SensorDetailsViewModel
    private lateinit var sensorAddEditViewModel: SensorAddEditViewModel
    private lateinit var actuatorListViewModel: ActuatorListViewModel
    private lateinit var actuatorDetailsViewModel: ActuatorDetailsViewModel
    private lateinit var actuatorAddEditViewModel: ActuatorAddEditViewModel
    private lateinit var mapViewModel: MapViewModel

    init {
        mainDashboardViewModelSingleton()
        networkListViewModelSingleton()
        sensorListViewModelSingleton()
        actuatorListViewModelSingleton()
        mapViewModelSingleton()
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return if (modelClass.isAssignableFrom(MainDashboardViewModel::class.java)) {
            mainDashboardViewModelSingleton() as T
        } else if (modelClass.isAssignableFrom(NetworkListViewModel::class.java)) {
            networkListViewModelSingleton() as T
        } else if (modelClass.isAssignableFrom(NetworkDetailsViewModel::class.java)) {
            networkDetailsViewModelSingleton() as T
        } else if (modelClass.isAssignableFrom(NetworkAddEditViewModel::class.java)) {
            networkAddEditViewModelSingleton() as T
        } else if (modelClass.isAssignableFrom(SensorListViewModel::class.java)) {
            sensorListViewModelSingleton() as T
        } else if (modelClass.isAssignableFrom(SensorDetailsViewModel::class.java)) {
            sensorDetailsViewModelSingleton() as T
        } else if (modelClass.isAssignableFrom(SensorAddEditViewModel::class.java)) {
            sensorAddEditViewModelSingleton() as T
        } else if (modelClass.isAssignableFrom(ActuatorListViewModel::class.java)) {
            actuatorListViewModelSingleton() as T
        } else if (modelClass.isAssignableFrom(ActuatorDetailsViewModel::class.java)) {
            actuatorDetailsViewModelSingleton() as T
        } else if (modelClass.isAssignableFrom(ActuatorAddEditViewModel::class.java)) {
            actuatorAddEditViewModelSingleton() as T
        } else if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            mapViewModelSingleton() as T
        } else {
            throw ClassCastException("No view model class recognized")
        }
    }

    private fun mainDashboardViewModelSingleton(): MainDashboardViewModel {
        if (mainDashboardViewModel == null)
            mainDashboardViewModel = MainDashboardViewModel(
                application,
                sensorManagementUseCase, actuatorManagementUseCase,
                dataManagementUseCase, logsManagementUseCase
            )
        return mainDashboardViewModel
    }

    private fun networkListViewModelSingleton(): NetworkListViewModel {
        if (networkListViewModel == null)
            networkListViewModel = NetworkListViewModel(
                application,
                networkManagementUseCase
            )
        return networkListViewModel
    }

    private fun networkDetailsViewModelSingleton(): NetworkDetailsViewModel {
        if (networkDetailsViewModel == null)
            networkDetailsViewModel = NetworkDetailsViewModel(
                application,
                networkManagementUseCase,
                logsManagementUseCase
            )
        return networkDetailsViewModel
    }

    private fun networkAddEditViewModelSingleton(): NetworkAddEditViewModel {
        if (networkAddEditViewModel == null)
            networkAddEditViewModel = NetworkAddEditViewModel(application, networkManagementUseCase)
        return networkAddEditViewModel
    }

    private fun sensorListViewModelSingleton(): SensorListViewModel {
        if (sensorListViewModel == null)
            sensorListViewModel = SensorListViewModel(
                application,
                sensorManagementUseCase
            )
        return sensorListViewModel
    }

    private fun sensorDetailsViewModelSingleton(): SensorDetailsViewModel {
        if (sensorDetailsViewModel == null)
            sensorDetailsViewModel = SensorDetailsViewModel(
                application, sensorManagementUseCase,
                dataManagementUseCase, logsManagementUseCase
            )
        return sensorDetailsViewModel
    }

    private fun sensorAddEditViewModelSingleton(): SensorAddEditViewModel {
        if (sensorAddEditViewModel == null)
            sensorAddEditViewModel = SensorAddEditViewModel(
                application,
                networkManagementUseCase,
                sensorManagementUseCase
            )
        return sensorAddEditViewModel
    }

    private fun actuatorListViewModelSingleton(): ActuatorListViewModel {
        if (actuatorListViewModel == null)
            actuatorListViewModel = ActuatorListViewModel(
                application,
                actuatorManagementUseCase
            )
        return actuatorListViewModel
    }

    private fun actuatorDetailsViewModelSingleton(): ActuatorDetailsViewModel {
        if (actuatorDetailsViewModel == null)
            actuatorDetailsViewModel = ActuatorDetailsViewModel(
                application, actuatorManagementUseCase,
                dataManagementUseCase, logsManagementUseCase
            )
        return actuatorDetailsViewModel
    }

    private fun actuatorAddEditViewModelSingleton(): ActuatorAddEditViewModel {
        if (actuatorAddEditViewModel == null)
            actuatorAddEditViewModel = ActuatorAddEditViewModel(
                application,
                networkManagementUseCase,
                actuatorManagementUseCase
            )
        return actuatorAddEditViewModel
    }

    private fun mapViewModelSingleton(): MapViewModel {
        if (mapViewModel == null)
            mapViewModel = MapViewModel(
                application, sensorManagementUseCase,
                actuatorManagementUseCase
            )
        return mapViewModel
    }

}
