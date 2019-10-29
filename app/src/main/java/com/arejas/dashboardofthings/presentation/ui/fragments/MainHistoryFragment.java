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
import com.arejas.dashboardofthings.databinding.CardMaindashboardHistoryBinding;
import com.arejas.dashboardofthings.databinding.FragmentMainsensorsBinding;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MainDashboardViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.helpers.HistoryChartHelper;
import com.arejas.dashboardofthings.utils.Enumerators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class MainHistoryFragment extends Fragment {

    FragmentMainsensorsBinding uiBinding;

    @Inject
    ViewModelFactory viewModelFactory;

    private MainDashboardViewModel mainHistoryViewModel;

    private LiveData<Resource<List<SensorExtended>>> currentListShown;
    private GridLayoutManager glm_grid;
    private MainHistoryFragment.HistoryListAdapter mAdapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Inject dependencies
        AndroidSupportInjection.inject(this);
        // Get the main dashboard activity view model and observe the changes in the details
        mainHistoryViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()), viewModelFactory).get(MainDashboardViewModel.class);
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
        currentListShown = mainHistoryViewModel.getListOfSensorsMainDashboard(refreshData);
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
                        List<SensorExtended> dataToSet = mAdapter.getData();
                        if (dataToSet != null) {
                            dataToSet.clear();
                        } else {
                            dataToSet = new ArrayList<>();
                        }
                        for (SensorExtended sensor : listResource.getData()) {
                            if (!sensor.getDataType().equals(Enumerators.DataType.STRING)) {
                                dataToSet.add(sensor);
                            }
                        }
                        updateList(dataToSet);
                        uiBinding.srlRefreshLayout.setRefreshing(false);
                        showList();
                    }
                }
            });
        }
    }

    private void updateList(List<SensorExtended> newList) {
        if ((newList != null) && (!newList.isEmpty())) {
            showList();
            mAdapter.setData(newList);
            mAdapter.notifyDataSetChanged();
        } else {
            showNoElements();
        }
    }

    private void configureListAdapter() {

        /* Get number of items in a row*/
        int iElementsPerRow = getResources().getInteger(R.integer.list_maindash_history_column_count);

        // Configure recycler view with a grid layout
        glm_grid = new GridLayoutManager(getContext(), iElementsPerRow);
        uiBinding.sensorsMainListListLayout.mainList.setLayoutManager(glm_grid);

        // Configure adapter for recycler view
        mAdapter = new MainHistoryFragment.HistoryListAdapter();
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

    class HistoryListAdapter extends RecyclerView.Adapter<MainHistoryFragment.HistoryListAdapter.HistoryListViewHolder> {

        private List<SensorExtended> mData;
        private Map<Integer, Integer> mSpinnerOptionSelectedPerSensor;

        public List<SensorExtended> getData() {
            return mData;
        }

        public void setData(List<SensorExtended> mData) {
            this.mData = mData;
            if (mData != null) {
                if (mSpinnerOptionSelectedPerSensor != null) {
                    mSpinnerOptionSelectedPerSensor.clear();
                } else {
                    mSpinnerOptionSelectedPerSensor = new HashMap<>();
                }
                for (Sensor sensor : mData) {
                    mSpinnerOptionSelectedPerSensor.put(sensor.getId(), 0);
                }
            } else {
                mSpinnerOptionSelectedPerSensor = null;
            }
        }

        @Override
        public MainHistoryFragment.HistoryListAdapter.HistoryListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            CardMaindashboardHistoryBinding binding = DataBindingUtil.inflate(inflater, R.layout.card_maindashboard_history,
                    parent, false);
            return new MainHistoryFragment.HistoryListAdapter.HistoryListViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final MainHistoryFragment.HistoryListAdapter.HistoryListViewHolder holder, int position) {
            if ((mData != null) && (mData.size() > position)) {
                holder.itemView.setTag(position);
                Sensor sensor = mData.get(position);
                if ((sensor != null) && (mSpinnerOptionSelectedPerSensor != null) && (mSpinnerOptionSelectedPerSensor.containsKey(sensor.getId()))) {
                    if (sensor.getDataType().equals(Enumerators.DataType.STRING)) {
                        holder.binding.lcHistorySpinnerCard.setVisibility(View.GONE);
                        holder.binding.lcHistoryChartCard.setVisibility(View.GONE);
                    } else if (sensor.getDataType().equals(Enumerators.DataType.BOOLEAN)) {
                        mSpinnerOptionSelectedPerSensor.put(sensor.getId(), HistoryChartHelper.SPINNER_HISTORY_LASTVAL);
                        holder.binding.setSpinnerSelected(HistoryChartHelper.SPINNER_HISTORY_LASTVAL);
                        holder.binding.lcHistorySpinnerCard.setSelection(HistoryChartHelper.SPINNER_HISTORY_LASTVAL);
                        holder.binding.lcHistorySpinnerCard.setVisibility(View.GONE);
                    } else {
                        holder.binding.setSpinnerSelected(mSpinnerOptionSelectedPerSensor.get(sensor.getId()));
                        holder.binding.lcHistorySpinnerCard.setSelection(mSpinnerOptionSelectedPerSensor.get(sensor.getId()));
                        holder.binding.lcHistorySpinnerCard.setVisibility(View.VISIBLE);
                    }
                    holder.setSensor(sensor);
                    holder.binding.setSensor(sensor);
                    holder.requestHistoryData();
                }
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

        class HistoryListViewHolder extends RecyclerView.ViewHolder implements HistorySpinnerChangeListener {

            private Sensor sensor;

            final CardMaindashboardHistoryBinding binding;

            private LiveData<Resource<List<DataValue>>> dataToObserve;

            HistoryListViewHolder(CardMaindashboardHistoryBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
                binding.setPresenter(this);
            }

            public Sensor getSensor() {
                return sensor;
            }

            public void setSensor(Sensor sensor) {
                this.sensor = sensor;
            }

            @Override
            public void onSpinnerItemSelected(int position) {
                if (sensor != null) {
                    mSpinnerOptionSelectedPerSensor.put(sensor.getId(), position);
                    requestHistoryData();
                }
            }

            public void requestHistoryData() {
                if (sensor != null) {
                    int position = mSpinnerOptionSelectedPerSensor.get(sensor.getId());
                    if (dataToObserve != null)
                        dataToObserve.removeObservers(MainHistoryFragment.this);
                    dataToObserve = mainHistoryViewModel.getHistoricalValue(sensor.getId(), position);
                    dataToObserve.observe(MainHistoryFragment.this, listResource -> {
                        if (listResource == null) {
                            showErrorCard();
                        } else {
                            if (listResource.getStatus() == Resource.Status.ERROR) {
                                showErrorCard();
                            } else if (listResource.getStatus() == Resource.Status.LOADING) {
                                showLoadingCard();
                            } else {
                                showDataCard();
                                binding.setSpinnerSelected(position);
                                binding.setData(listResource.getData());
                            }
                        }
                    });
                }
            }

            private void showDataCard() {
                if (binding != null) {
                    binding.historyDataLayout.setVisibility(View.VISIBLE);
                    binding.historyErrorLayout.errorLayout.setVisibility(View.GONE);
                    binding.historyLoadingLayout.loadingLayout.setVisibility(View.GONE);
                }
            }

            private void showErrorCard() {
                if (binding != null) {
                    binding.historyDataLayout.setVisibility(View.GONE);
                    binding.historyErrorLayout.errorLayout.setVisibility(View.VISIBLE);
                    binding.historyLoadingLayout.loadingLayout.setVisibility(View.GONE);
                }
            }

            private void showLoadingCard() {
                if (uiBinding != null) {
                    binding.historyDataLayout.setVisibility(View.GONE);
                    binding.historyErrorLayout.errorLayout.setVisibility(View.GONE);
                    binding.historyLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
                }
            }

        }

    }

    public interface HistorySpinnerChangeListener {

        public void onSpinnerItemSelected (int position);

    }
    
}
