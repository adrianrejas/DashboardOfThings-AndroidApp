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
import com.arejas.dashboardofthings.databinding.FragmentMainlogsBinding;
import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MainDashboardViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class MainLogsFragment extends Fragment {

    FragmentMainlogsBinding uiBinding;

    @Inject
    ViewModelFactory viewModelFactory;

    private MainDashboardViewModel mainnetwork_addeditViewModel;

    private LiveData<Resource<List<Log>>> currentListShown;
    private LinearLayoutManager llm_linear;
    private MainLogsFragment.LogsListAdapter mAdapter;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Inject dependencies
        AndroidSupportInjection.inject(this);
        // Get the movie activity view model and observe the changes in the details
        mainnetwork_addeditViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()), viewModelFactory).get(MainDashboardViewModel.class);
        setList(true, false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        uiBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_mainlogs, container, false);
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
        currentListShown = mainnetwork_addeditViewModel.getLastSensorNotificationLogsInMainDashboard(refreshData);
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
        uiBinding.logsMainListListLayout.mainList.setLayoutManager(llm_linear);

        // Configure adapter for recycler view
        mAdapter = new MainLogsFragment.LogsListAdapter();
        uiBinding.logsMainListListLayout.mainList.setAdapter(mAdapter);
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private void showError() {
        if (uiBinding != null) {
            uiBinding.logsMainListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.logsMainListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.logsMainListErrorLayout.errorLayout.setVisibility(View.VISIBLE);
            uiBinding.logsMainListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
            uiBinding.logsMainListErrorLayout.tvError.setText(getString(R.string.error_in_list));
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.logsMainListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.logsMainListLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
            uiBinding.logsMainListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.logsMainListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Show the fragment info has no elements.
     */
    private void showNoElements() {
        if (uiBinding != null) {
            uiBinding.logsMainListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.logsMainListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.logsMainListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.logsMainListNoElementsLayout.noElementsLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the fragment info with list.
     */
    private void showList() {
        if (uiBinding != null) {
            uiBinding.logsMainListListLayout.listLayout.setVisibility(View.VISIBLE);
            uiBinding.logsMainListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.logsMainListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.logsMainListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    class LogsListAdapter extends RecyclerView.Adapter<MainLogsFragment.LogsListAdapter.LogListViewHolder> {

        private List<Log> mData;

        public List<Log> getData() {
            return mData;
        }

        public void setData(List<Log> mData) {
            this.mData = mData;
        }

        @Override
        public MainLogsFragment.LogsListAdapter.LogListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            CardLogBinding binding = DataBindingUtil.inflate(inflater, R.layout.card_log,
                    parent, false);
            return new MainLogsFragment.LogsListAdapter.LogListViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final MainLogsFragment.LogsListAdapter.LogListViewHolder holder, int position) {
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
