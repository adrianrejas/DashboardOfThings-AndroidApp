package com.arejas.dashboardofthings.domain.usecases;

import androidx.lifecycle.LiveData;

import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface NetworkManagementUseCase extends BaseUseCase {

    public LiveData<Resource<List<NetworkExtended>>> getListOfNetworks();

    public LiveData<Resource<NetworkExtended>> getNetwork(@NotNull Integer networkId);

    public LiveData<Resource> createNetwork(@NotNull Network network);

    public LiveData<Resource> updateNetwork(@NotNull Network network);

    public LiveData<Resource> deleteNetwork(@NotNull Network network);

}
