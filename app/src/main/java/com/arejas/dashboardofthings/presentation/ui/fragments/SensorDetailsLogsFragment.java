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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.CardLogBinding;
import com.arejas.dashboardofthings.databinding.FragmentSensorDetailsLogsBinding;
import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class SensorDetailsLogsFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String SENSOR_ID = "sensor_id";

    FragmentSensorDetailsLogsBinding uiBinding;

    @Inject
    ViewModelFactory viewModelFactory;

    private SensorDetailsViewModel sensorDetailsViewModel;

    public Integer sensorId;
    
    private LiveData<Resource<List<Log>>> currentListShown;
    private LinearLayoutManager llm_linear;
    private SensorDetailsLogsFragment.LogsListAdapter mAdapter;

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
            setList(true, false);
        } else {
            showError();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        uiBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sensor_details_logs, container, false);
        // Configure adapter for recycler view
        configureListAdapter();
        // Set action of refreshing list when refreshing gesture detected
        uiBinding.srlRefreshLayout.setOnRefreshListener(() -> setList(false, true));
        return uiBinding.getRoot();
    }

    /**
     * Function called for setting a new list, requesting it to the Database
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
        currentListShown = sensorDetailsViewModel.getLogsForSensor(refreshData);
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
    }

    private void updateList(List<Log> newList) {
        if ((newList != null) && (!newList.isEmpty())) {
            showList();
            mAdapter.setData(newList);
            mAdapter.notifyDataSetChanged();
        } else {
            showNoElements();
        }
    }

    private void configureListAdapter() {
        // Configure recycler view with a grid layout
        llm_linear = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        uiBinding.logsSensorDetailsListLayout.mainList.setLayoutManager(llm_linear);

        // Configure adapter for recycler view
        mAdapter = new SensorDetailsLogsFragment.LogsListAdapter();
        uiBinding.logsSensorDetailsListLayout.mainList.setAdapter(mAdapter);
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private void showError() {
        if (uiBinding != null) {
            uiBinding.logsSensorDetailsListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.logsSensorDetailsLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.logsSensorDetailsErrorLayout.errorLayout.setVisibility(View.VISIBLE);
            uiBinding.logsSensorDetailsNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
            uiBinding.logsSensorDetailsErrorLayout.tvError.setText(getString(R.string.error_in_list));
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.logsSensorDetailsListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.logsSensorDetailsLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
            uiBinding.logsSensorDetailsErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.logsSensorDetailsNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Show the fragment info has no elements.
     */
    private void showNoElements() {
        if (uiBinding != null) {
            uiBinding.logsSensorDetailsListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.logsSensorDetailsLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.logsSensorDetailsErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.logsSensorDetailsNoElementsLayout.noElementsLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the fragment info with list.
     */
    private void showList() {
        if (uiBinding != null) {
            uiBinding.logsSensorDetailsListLayout.listLayout.setVisibility(View.VISIBLE);
            uiBinding.logsSensorDetailsLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.logsSensorDetailsErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.logsSensorDetailsNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    class LogsListAdapter extends RecyclerView.Adapter<SensorDetailsLogsFragment.LogsListAdapter.LogListViewHolder> {

        private List<Log> mData;

        public List<Log> getData() {
            return mData;
        }

        public void setData(List<Log> mData) {
            this.mData = mData;
        }

        @Override
        public SensorDetailsLogsFragment.LogsListAdapter.LogListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            CardLogBinding binding = DataBindingUtil.inflate(inflater, R.layout.card_log,
                    parent, false);
            return new SensorDetailsLogsFragment.LogsListAdapter.LogListViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final SensorDetailsLogsFragment.LogsListAdapter.LogListViewHolder holder, int position) {
            if ((mData != null) && (mData.size() > position)) {
                holder.itemView.setTag(position);
                holder.binding.setLog(mData.get(position));
            }
        }

        @Override
        public int getItemCount() {
            if (mData != null) {
                return mData.size();
            } else {
                return 0;
            }
        }

        class LogListViewHolder extends RecyclerView.ViewHolder {

            final CardLogBinding binding;

            LogListViewHolder(CardLogBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

    }


}
