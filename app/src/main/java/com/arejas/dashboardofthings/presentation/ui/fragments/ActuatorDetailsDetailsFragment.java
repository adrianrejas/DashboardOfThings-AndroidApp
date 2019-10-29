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
import com.arejas.dashboardofthings.databinding.FragmentActuatorDetailsDetailsBinding;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorDetailsActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorListActivity;
import com.arejas.dashboardofthings.presentation.ui.helpers.HistoryChartHelper;
import com.arejas.dashboardofthings.presentation.ui.helpers.ActuatorDetailsListener;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * A fragment representing a single Actuator detail screen.
 * This fragment is either contained in a {@link ActuatorListActivity}
 * in two-pane mode (on tablets) or a {@link ActuatorDetailsActivity}
 * on handsets.
 */
public class ActuatorDetailsDetailsFragment extends Fragment implements ActuatorDetailsListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ACTUATOR_ID = "actuator_id";

    @Inject
    ViewModelFactory viewModelFactory;

    FragmentActuatorDetailsDetailsBinding uiBinding;

    public Integer actuatorId;

    private ActuatorDetailsViewModel actuatorDetailsViewModel;

    private LiveData<Resource<ActuatorExtended>> currentInfoShown;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ACTUATOR_ID)) {
            actuatorId = getArguments().getInt(ACTUATOR_ID);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Inject dependencies
        AndroidSupportInjection.inject(this);
        if (actuatorId != null) {
            // Get the actuator details activity view model and observe the changes in the details
            actuatorDetailsViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()), viewModelFactory).get(ActuatorDetailsViewModel.class);
            actuatorDetailsViewModel.setActuatorId(actuatorId);
            setData(true, false);
        } else {
            showError();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        uiBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_actuator_details_details, container, false);
        uiBinding.setPresenter(this);
        // Set action of refreshing list when refreshing gesture detected
        uiBinding.srlRefreshLayout.setOnRefreshListener(() -> setData(false, true));
        return uiBinding.getRoot();
    }

    /**
     * Function called for setting new data, requesting it to the Database
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
        currentInfoShown = actuatorDetailsViewModel.getActuator(refreshData);
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
    }

    private void updateData(ActuatorExtended actuator) {
        if (actuator != null) {
            showData();
            uiBinding.setActuator(actuator);
        }
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private void showError() {
        if (uiBinding != null) {
            uiBinding.actuatorDetailsLayout.setVisibility(View.GONE);
            uiBinding.actuatorDetailsLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.actuatorDetailsErrorLayout.errorLayout.setVisibility(View.VISIBLE);
            uiBinding.actuatorDetailsErrorLayout.tvError.setText(getString(R.string.error_in_list));
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.actuatorDetailsLayout.setVisibility(View.GONE);
            uiBinding.actuatorDetailsLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
            uiBinding.actuatorDetailsErrorLayout.errorLayout.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show the fragment info with data.
     */
    private void showData() {
        if (uiBinding != null) {
            uiBinding.actuatorDetailsLayout.setVisibility(View.VISIBLE);
            uiBinding.actuatorDetailsLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.actuatorDetailsErrorLayout.errorLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void sendInteger() {
        try {
            if ((currentInfoShown != null) && (currentInfoShown.getValue() != null) &&
                    (currentInfoShown.getValue().getStatus().equals(Resource.Status.SUCCESS))){
                String data = uiBinding.etActuatorDetailIntegerValue.getText().toString();
                Actuator actuator = currentInfoShown.getValue().getData();
                Float comparingData = Float.valueOf(data);
                if ((actuator.getDataNumberMinimum() != null) && (actuator.getDataNumberMaximum() != null)) {
                    if ((comparingData < actuator.getDataNumberMinimum()) || ((comparingData > actuator.getDataNumberMaximum()))) {
                        ToastHelper.showToast(getString(R.string.set_toast_between,
                                actuator.getDataNumberMinimum().toString(),
                                actuator.getDataNumberMaximum().toString()));
                        return;
                    }
                } else if (actuator.getDataNumberMinimum() != null) {
                    if (comparingData < actuator.getDataNumberMinimum()) {
                        ToastHelper.showToast(getString(R.string.set_toast_smaller_than,
                                actuator.getDataNumberMinimum().toString()));
                        return;
                    }
                } else if (actuator.getDataNumberMaximum() != null) {
                    if (comparingData > actuator.getDataNumberMaximum()) {
                        ToastHelper.showToast(getString(R.string.set_toast_bigger_than,
                                actuator.getDataNumberMaximum().toString()));
                        return;
                    }
                }
                if (data != null)
                    actuatorDetailsViewModel.sendActuatorData(data);
            }
        } catch (Exception e) {
            ToastHelper.showToast(getString(R.string.set_toast_error));
        }
    }

    @Override
    public void sendFloat() {
        try {
            if ((currentInfoShown != null) && (currentInfoShown.getValue() != null) &&
                    (currentInfoShown.getValue().getStatus().equals(Resource.Status.SUCCESS))) {
                String data = uiBinding.etActuatorDetailDecimalValue.getText().toString();
                Actuator actuator = currentInfoShown.getValue().getData();
                Float comparingData = Float.valueOf(data);
                if ((actuator.getDataNumberMinimum() != null) && (actuator.getDataNumberMaximum() != null)) {
                    if ((comparingData < actuator.getDataNumberMinimum()) || ((comparingData > actuator.getDataNumberMaximum()))) {
                        ToastHelper.showToast(getString(R.string.set_toast_between,
                                actuator.getDataNumberMinimum().toString(),
                                actuator.getDataNumberMaximum().toString()));
                        return;
                    }
                } else if (actuator.getDataNumberMinimum() != null) {
                    if (comparingData < actuator.getDataNumberMinimum()) {
                        ToastHelper.showToast(getString(R.string.set_toast_smaller_than,
                                actuator.getDataNumberMinimum().toString()));
                        return;
                    }
                } else if (actuator.getDataNumberMaximum() != null) {
                    if (comparingData > actuator.getDataNumberMaximum()) {
                        ToastHelper.showToast(getString(R.string.set_toast_bigger_than,
                                actuator.getDataNumberMaximum().toString()));
                        return;
                    }
                }
                if (data != null)
                    actuatorDetailsViewModel.sendActuatorData(data);
            }
        } catch (Exception e) {
            ToastHelper.showToast(getString(R.string.set_toast_error));
        }
    }

    @Override
    public void sendBooleanFalse() {
        if ((currentInfoShown != null) && (currentInfoShown.getValue() != null) &&
                (currentInfoShown.getValue().getStatus().equals(Resource.Status.SUCCESS))){
            actuatorDetailsViewModel.sendActuatorData("false");
        }
    }

    @Override
    public void sendBooleanTrue() {
        if ((currentInfoShown != null) && (currentInfoShown.getValue() != null) &&
                (currentInfoShown.getValue().getStatus().equals(Resource.Status.SUCCESS))){
            actuatorDetailsViewModel.sendActuatorData("true");
        }
    }

    @Override
    public void sendString() {
        if ((currentInfoShown != null) && (currentInfoShown.getValue() != null) &&
                (currentInfoShown.getValue().getStatus().equals(Resource.Status.SUCCESS))){
            String data = uiBinding.etActuatorDetailStringValue.getText().toString();
            if (data != null)
                actuatorDetailsViewModel.sendActuatorData(data);
        }
    }

}
