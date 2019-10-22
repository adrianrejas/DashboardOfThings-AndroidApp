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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.CardMaindashboardSensorBinding;
import com.arejas.dashboardofthings.databinding.FragmentMainsensorsBinding;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MainDashboardViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

public class MainSensorsFragment extends Fragment {

    FragmentMainsensorsBinding uiBinding;

    @Inject
    ViewModelFactory viewModelFactory;

    private MainDashboardViewModel mainDashoardViewModel;

    private LiveData<Resource<List<SensorExtended>>> currentListShown;
    private LiveData<Resource<List<DataValue>>> dataValuesReceivedManaged;
    private GridLayoutManager glm_grid;
    private SensorsListAdapter mAdapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Get the main dashboard activity view model and observe the changes in the details
        mainDashoardViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()), viewModelFactory).get(MainDashboardViewModel.class);
        setList(true, false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        uiBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_mainsensors, container, false);
        // Configure adapter for recycler view
        configureListAdapter();
        // Set action of refreshing list when refreshing gesture detected
        uiBinding.srlRefreshLayout.setOnRefreshListener(() -> setList(false, true));
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
    private void setList(boolean showLoading, boolean refreshData) {
        if (showLoading) {
            showLoading();
        }
        if (currentListShown != null) {
            currentListShown.removeObservers(this);
            currentListShown = null;
        }
        currentListShown = mainDashoardViewModel.getListOfSensorsMainDashboard(refreshData);
        if (currentListShown != null) {
            currentListShown.observe(this, listResource -> {
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
                        updateList(listResource.getData());
                        uiBinding.srlRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
        if (dataValuesReceivedManaged != null) {
            dataValuesReceivedManaged.removeObservers(this);
            dataValuesReceivedManaged = null;
        }
        dataValuesReceivedManaged = mainDashoardViewModel.getListOfSensorsInDashboardLastValues(refreshData);
        if (dataValuesReceivedManaged != null) {
            dataValuesReceivedManaged.observe(this, listResource -> {
                if (listResource == null) {
                    showError();
                } else {
                    if ((listResource.getStatus() == Resource.Status.SUCCESS) &&
                            (listResource.getData() != null)) {
                        updateDataValues(listResource.getData());
                    }
                }
            });
        }
    }

    private void updateList(List<SensorExtended> newList) {
        if ((newList != null) && (!newList.isEmpty())) {
            showList();
            mAdapter.setSensors(newList);
            mAdapter.notifyDataSetChanged();
        } else {
            showNoElements();
        }
    }

    private void updateDataValues(List<DataValue> newDataValueList) {
        Map<Integer, DataValue> newDataValueMap = mAdapter.getDataValues();
        if (newDataValueMap != null) {
            newDataValueMap.clear();
        } else {
            newDataValueMap = new HashMap<>();
        }
        for (DataValue value : newDataValueList) {
            newDataValueMap.put(value.getSensorId(), value);
        }
        mAdapter.setDataValues(newDataValueMap);
        mAdapter.notifyDataSetChanged();
    }

    private void configureListAdapter() {

        /* Get number of items in a row*/
        int iElementsPerRow = getResources().getInteger(R.integer.list_maindash_column_count);

        // Configure recycler view with a grid layout
        glm_grid = new GridLayoutManager(getContext(), iElementsPerRow);
        uiBinding.sensorsMainListListLayout.mainList.setLayoutManager(glm_grid);

        // Configure adapter for recycler view
        mAdapter = new SensorsListAdapter();
        uiBinding.sensorsMainListListLayout.mainList.setAdapter(mAdapter);
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private void showError() {
        if (uiBinding != null) {
            uiBinding.sensorsMainListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.sensorsMainListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.sensorsMainListErrorLayout.errorLayout.setVisibility(View.VISIBLE);
            uiBinding.sensorsMainListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
            uiBinding.sensorsMainListErrorLayout.tvError.setText(getString(R.string.error_in_list));
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.sensorsMainListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.sensorsMainListLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
            uiBinding.sensorsMainListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.sensorsMainListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Show the fragment info has no elements.
     */
    private void showNoElements() {
        if (uiBinding != null) {
            uiBinding.sensorsMainListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.sensorsMainListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.sensorsMainListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.sensorsMainListNoElementsLayout.noElementsLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the fragment info with list.
     */
    private void showList() {
        if (uiBinding != null) {
            uiBinding.sensorsMainListListLayout.listLayout.setVisibility(View.VISIBLE);
            uiBinding.sensorsMainListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.sensorsMainListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.sensorsMainListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    class SensorsListAdapter extends RecyclerView.Adapter<SensorsListAdapter.SensorListViewHolder> {

        private List<SensorExtended> mSensors;

        private Map<Integer, DataValue> mDataValues;

        public List<SensorExtended> getSensors() {
            return mSensors;
        }

        public void setSensors(List<SensorExtended> mData) {
            this.mSensors = mData;
        }

        public Map<Integer, DataValue> getDataValues() {
            return mDataValues;
        }

        public void setDataValues(Map<Integer, DataValue> mDataValues) {
            this.mDataValues = mDataValues;
        }

        @Override
        public SensorListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            CardMaindashboardSensorBinding binding = DataBindingUtil.inflate(inflater, R.layout.card_maindashboard_sensor,
                    parent, false);
            return new SensorListViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final SensorListViewHolder holder, int position) {
            if ((mSensors != null) && (mSensors.size() > position)) {
                holder.itemView.setTag(position);
                SensorExtended sensor = mSensors.get(position);
                if (sensor != null) {
                    holder.setSensor(sensor);
                    holder.binding.setSensor(sensor);
                    holder.binding.setPresenter(holder);
                    if ((mDataValues != null) && mDataValues.containsKey(sensor.getId())) {
                        holder.binding.setLastValue(mDataValues.get(sensor.getId()).getValue());
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            if (mSensors != null) {
                return mSensors.size();
            } else {
                return 0;
            }
        }

        class SensorListViewHolder extends RecyclerView.ViewHolder implements SensorMainDashboardListener {

            Sensor sensor;

            final CardMaindashboardSensorBinding binding;

            SensorListViewHolder(CardMaindashboardSensorBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public Sensor getSensor() {
                return sensor;
            }

            public void setSensor(Sensor sensor) {
                this.sensor = sensor;
            }

            @Override
            public void requestReload() {
                if (sensor != null) {
                    mainDashoardViewModel.requestSensorReload(sensor);
                }
            }
        }

    }

    public interface SensorMainDashboardListener {

        public void requestReload ();

    }

}
