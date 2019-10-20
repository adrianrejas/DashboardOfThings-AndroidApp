package com.arejas.dashboardofthings.domain.usecases.implementations;

import androidx.lifecycle.LiveData;

import com.arejas.dashboardofthings.data.interfaces.DotRepository;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ActuatorManagementUseCaseImpl implements ActuatorManagementUseCase {

    private final DotRepository repository;

    public ActuatorManagementUseCaseImpl(DotRepository repository) {
        this.repository = repository;
    }

    @Override
    public LiveData<Resource<List<ActuatorExtended>>> getListOfActuators() {
        return repository.getListOfActuators();
    }

    public LiveData<Resource<List<ActuatorExtended>>> getListOfActuatorsMainDashboard() {
        return repository.getListOfActuatorsMainDashboard();
    }

    public LiveData<Resource<List<ActuatorExtended>>> getListOfActuatorsLocated() {
        return repository.getListOfActuatorsLocated();
    }

    @Override
    public LiveData<Resource<ActuatorExtended>> getActuator(@NotNull Integer actuatorId) {
        return repository.getActuator(actuatorId);
    }

    @Override
    public LiveData<Resource> createActuator(@NotNull Actuator actuator) {
        return repository.createActuator(actuator);
    }

    @Override
    public LiveData<Resource> updateActuator(@NotNull Actuator actuator) {
        return repository.updateActuator(actuator);
    }

    @Override
    public LiveData<Resource> deleteActuator(@NotNull Actuator actuator) {
        return repository.deleteActuator(actuator);
    }
}
