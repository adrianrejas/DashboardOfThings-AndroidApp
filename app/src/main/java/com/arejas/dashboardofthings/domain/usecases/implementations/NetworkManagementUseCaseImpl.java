package com.arejas.dashboardofthings.domain.usecases.implementations;

import androidx.lifecycle.LiveData;

import com.arejas.dashboardofthings.data.interfaces.DotRepository;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.domain.usecases.NetworkManagementUseCase;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NetworkManagementUseCaseImpl implements NetworkManagementUseCase {

    private final DotRepository repository;

    public NetworkManagementUseCaseImpl(DotRepository repository) {
        this.repository = repository;
    }

    @Override
    public LiveData<Resource<List<NetworkExtended>>> getListOfNetworks() {
        return repository.getListOfNetworks();
    }

    @Override
    public LiveData<Resource<NetworkExtended>> getNetwork(@NotNull Integer networkId) {
        return repository.getNetwork(networkId);
    }

    @Override
    public LiveData<Resource> createNetwork(@NotNull Network network) {
        return repository.createNetwork(network);
    }

    @Override
    public LiveData<Resource> updateNetwork(@NotNull Network network) {
        return repository.updateNetwork(network);
    }

    @Override
    public LiveData<Resource> deleteNetwork(@NotNull Network network) {
        return repository.deleteNetwork(network);
    }
}
