package com.arejas.dashboardofthings.domain.usecases;

import androidx.lifecycle.LiveData;

import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ActuatorManagementUseCase extends BaseUseCase {

    public LiveData<List<ActuatorExtended>> getListOfActuators();

    public LiveData<List<ActuatorExtended>> getListOfActuatorsMainDashboard();

    public LiveData<List<ActuatorExtended>> getListOfActuatorsLocated();

    public LiveData<ActuatorExtended> getActuator(@NotNull Integer actuatorId);

    public LiveData<Resource> createActuator(@NotNull Actuator actuator);

    public LiveData<Resource> updateActuator(@NotNull Actuator actuator);

    public LiveData<Resource> deleteActuator(@NotNull Actuator actuator);

}
