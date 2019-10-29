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
import com.arejas.dashboardofthings.databinding.FragmentSensorDetailsDetailsBinding;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.activities.SensorDetailsActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.SensorListActivity;
import com.arejas.dashboardofthings.presentation.ui.helpers.HistoryChartHelper;
import com.arejas.dashboardofthings.presentation.ui.helpers.SensorDetailsListener;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * A fragment representing a single Sensor detail screen.
 * This fragment is either contained in a {@link SensorListActivity}
 * in two-pane mode (on tablets) or a {@link SensorDetailsActivity}
 * on handsets.
 */
public class SensorDetailsDetailsFragment extends Fragment implements SensorDetailsListener {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String SENSOR_ID = "sensor_id";

    @Inject
    ViewModelFactory viewModelFactory;

    FragmentSensorDetailsDetailsBinding uiBinding;

    public Integer sensorId;

    private SensorDetailsViewModel sensorDetailsViewModel;

    private LiveData<Resource<SensorExtended>> currentInfoShown;
    private LiveData<Resource<DataValue>> lastDataReceived;
    private LiveData<Resource<List<DataValue>>> lastHistoryDataReceived;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(SENSOR_ID)) {
            sensorId = getArguments().getInt(SENSOR_ID);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Inject dependencies
        AndroidSupportInjection.inject(this);
        if (sensorId != null) {
            // Get the sensor details activity view model and observe the changes in the details
            sensorDetailsViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()), viewModelFactory).get(SensorDetailsViewModel.class);
            sensorDetailsViewModel.setSensorId(sensorId);
            setData(true, false);
        } else {
            showError();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        uiBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sensor_details_details, container, false);
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
        currentInfoShown = sensorDetailsViewModel.getSensor(refreshData);
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
        if (lastDataReceived != null) {
            lastDataReceived.removeObservers(this);
            lastDataReceived = null;
        }
        lastDataReceived = sensorDetailsViewModel.getLastValueForSensor(refreshData);
        if (lastDataReceived != null) {
            lastDataReceived.observe(this, resource -> {
                if (resource == null) {
                    showError();
                } else {
                    if ((resource.getStatus() == Resource.Status.SUCCESS) &&
                            (resource.getData() != null)) {
                        uiBinding.setLastValue(resource.getData().getValue());
                    }
                }
            });
        }
        reloadHistoryData(refreshData);
    }

    private void updateData(SensorExtended sensor) {
        if (sensor != null) {
            showData();
            uiBinding.setSensor(sensor);
            uiBinding.setSensor(sensor);if (sensor.getDataType().equals(Enumerators.DataType.STRING)) {
                uiBinding.spSensorDetailsHistorySpinner.setVisibility(View.GONE);
                uiBinding.lcSensorDetailsHistoryChart.setVisibility(View.GONE);
            } else if (sensor.getDataType().equals(Enumerators.DataType.BOOLEAN)) {
                uiBinding.setHistorySpinnerSelected(HistoryChartHelper.SPINNER_HISTORY_LASTVAL);
                uiBinding.spSensorDetailsHistorySpinner.setSelection(HistoryChartHelper.SPINNER_HISTORY_LASTVAL);
                uiBinding.spSensorDetailsHistorySpinner.setVisibility(View.GONE);
            } else {
                uiBinding.setHistorySpinnerSelected(sensorDetailsViewModel.getHistorySpinnerPosition());
                uiBinding.spSensorDetailsHistorySpinner.setSelection(sensorDetailsViewModel.getHistorySpinnerPosition());
                uiBinding.spSensorDetailsHistorySpinner.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private void showError() {
        if (uiBinding != null) {
            uiBinding.sensorDetailsLayout.setVisibility(View.GONE);
            uiBinding.sensorDetailsLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.sensorDetailsErrorLayout.errorLayout.setVisibility(View.VISIBLE);
            uiBinding.sensorDetailsErrorLayout.tvError.setText(getString(R.string.error_in_list));
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.sensorDetailsLayout.setVisibility(View.GONE);
            uiBinding.sensorDetailsLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
            uiBinding.sensorDetailsErrorLayout.errorLayout.setVisibility(View.GONE);
        }
    }
    
    /**
     * Show the fragment info with data.
     */
    private void showData() {
        if (uiBinding != null) {
            uiBinding.sensorDetailsLayout.setVisibility(View.VISIBLE);
            uiBinding.sensorDetailsLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.sensorDetailsErrorLayout.errorLayout.setVisibility(View.GONE);
        }
    }

    private void reloadHistoryData(boolean refreshData) {
        if (lastHistoryDataReceived != null)
            lastHistoryDataReceived.removeObservers(this);
        lastHistoryDataReceived = sensorDetailsViewModel.getHistoricalData(refreshData);
        lastHistoryDataReceived.observe(this, listResource -> {
            if (listResource == null) {
                showError();
            } else {
                if (listResource.getStatus() == Resource.Status.ERROR) {
                    showError();
                } else if (listResource.getStatus() == Resource.Status.LOADING) {
                    showLoading();
                } else {
                    showData();
                    uiBinding.setHistorySpinnerSelected(sensorDetailsViewModel.getHistorySpinnerPosition());
                    uiBinding.setHistoryData(listResource.getData());
                }
            }
        });
    }

    @Override
    public void onSpinnerItemSelected(int position) {
        if (sensorId != null) {
            sensorDetailsViewModel.setHistorySpinnerPosition(position);
            reloadHistoryData(false);
        }
    }

}
