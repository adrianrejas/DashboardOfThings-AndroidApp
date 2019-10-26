package com.arejas.dashboardofthings.presentation.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.FragmentNetworkDetailsDetailsBinding;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkDetailActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkListActivity;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * A fragment representing a single Network detail screen.
 * This fragment is either contained in a {@link NetworkListActivity}
 * in two-pane mode (on tablets) or a {@link NetworkDetailActivity}
 * on handsets.
 */
public class NetworkDetailDetailsFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String NETWORK_ID = "network_id";

    @Inject
    ViewModelFactory viewModelFactory;

    FragmentNetworkDetailsDetailsBinding uiBinding;

    public Integer networkId;

    private NetworkDetailsViewModel networkDetailsViewModel;

    private LiveData<Resource<NetworkExtended>> currentInfoShown;
    private LiveData<Resource<List<Sensor>>> currentRelatedSensors;
    private LiveData<Resource<List<Actuator>>> currentRelatedActuators;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(NETWORK_ID)) {
            networkId = getArguments().getInt(NETWORK_ID);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Inject dependencies
        AndroidSupportInjection.inject(this);
        if (networkId != null) {
            // Get the movie activity view model and observe the changes in the details
            networkDetailsViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()), viewModelFactory).get(NetworkDetailsViewModel.class);
            networkDetailsViewModel.setNetworkId(networkId);
            setData(true, false);
        } else {
            showError();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        uiBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_network_details_details, container, false);
        // Set action of refreshing list when refreshing gesture detected
        uiBinding.srlRefreshLayout.setOnRefreshListener(() -> setData(false, true));
        return uiBinding.getRoot();
    }

    /**
     * Function called for setting a new movie list, requesting it to the REST API
     *
     * @param showLoading true if wanted to show the loading layout until info got (we don't want in
     *                case of swipe refresh, because it has it's own way to info about the loading process).
     * @param refreshData true if wanted to reload the data, false if it's enough with cached data.
     *
     */
    private void setData(boolean showLoading, boolean refreshData) {
        if (showLoading) {
            showLoading();
        }
        if (currentInfoShown != null) {
            currentInfoShown.removeObservers(this);
            currentInfoShown = null;
        }
        currentInfoShown = networkDetailsViewModel.getNetwork(refreshData);
        if (currentInfoShown != null) {
            currentInfoShown.observe(this, listResource -> {
                if (listResource == null) {
                    showError();
                } else {
                    if (listResource.getStatus() == Resource.Status.ERROR) {
                        showError();
                        uiBinding.srlRefreshLayout.setRefreshing(false);
                    } else if (listResource.getStatus() == Resource.Status.LOADING) {
                        if (showLoading)
                            showLoading();
                    } else {
                        updateData(listResource.getData());
                        uiBinding.srlRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
        if (currentRelatedSensors != null) {
            currentRelatedSensors.removeObservers(this);
            currentRelatedSensors = null;
        }
        currentRelatedSensors = networkDetailsViewModel.getSensorsRelated(refreshData);
        if (currentRelatedSensors != null) {
            currentRelatedSensors.observe(this, listResource -> {
                if (listResource == null) {
                    showError();
                } else {
                    if ((listResource.getStatus() == Resource.Status.SUCCESS) &&
                            (listResource.getData() != null)) {
                        updateRelatedSensors(listResource.getData());
                    }
                }
            });
        }
        if (currentRelatedActuators != null) {
            currentRelatedActuators.removeObservers(this);
            currentRelatedActuators = null;
        }
        currentRelatedActuators = networkDetailsViewModel.getActuatorsRelated(refreshData);
        if (currentRelatedActuators != null) {
            currentRelatedActuators.observe(this, listResource -> {
                if (listResource == null) {
                    showError();
                } else {
                    if ((listResource.getStatus() == Resource.Status.SUCCESS) &&
                            (listResource.getData() != null)) {
                        updateRelatedActuators(listResource.getData());
                    }
                }
            });
        }
    }

    private void updateData(NetworkExtended network) {
        if (network != null) {
            showData();
            uiBinding.setNetwork(network);
        }
    }

    private void updateRelatedSensors(List<Sensor> sensors) {
        if (sensors != null) {
            uiBinding.setSensorList(sensors);
        }
    }

    private void updateRelatedActuators(List<Actuator> actuators) {
        if (actuators != null) {
            uiBinding.setActuatorList(actuators);
        }
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private void showError() {
        if (uiBinding != null) {
            uiBinding.networkDetailsLayout.setVisibility(View.GONE);
            uiBinding.networkDetailsLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.networkDetailsErrorLayout.errorLayout.setVisibility(View.VISIBLE);
            uiBinding.networkDetailsErrorLayout.tvError.setText(getString(R.string.error_in_list));
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.networkDetailsLayout.setVisibility(View.GONE);
            uiBinding.networkDetailsLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
            uiBinding.networkDetailsErrorLayout.errorLayout.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show the fragment info with data.
     */
    private void showData() {
        if (uiBinding != null) {
            uiBinding.networkDetailsLayout.setVisibility(View.VISIBLE);
            uiBinding.networkDetailsLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.networkDetailsErrorLayout.errorLayout.setVisibility(View.GONE);
        }
    }

}
