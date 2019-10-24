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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.CardMaindashboardLogBinding;
import com.arejas.dashboardofthings.databinding.CardMaindashboardStatusBinding;
import com.arejas.dashboardofthings.databinding.FragmentMainlogsBinding;
import com.arejas.dashboardofthings.databinding.FragmentMainstatusBinding;
import com.arejas.dashboardofthings.domain.entities.database.Log;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MainDashboardViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

public class MainStatusFragment extends Fragment {

    FragmentMainstatusBinding uiBinding;

    @Inject
    ViewModelFactory viewModelFactory;

    private MainDashboardViewModel mainnetwork_addeditViewModel;

    private LiveData<Resource<List<Log>>> currentListShown;
    private LinearLayoutManager llm_linear;
    private StatusListAdapter mAdapter;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Get the movie activity view model and observe the changes in the details
        mainnetwork_addeditViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()), viewModelFactory).get(MainDashboardViewModel.class);
        setList(true, false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        uiBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_mainstatus, container, false);
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

        /* Get number of items in a row*/
        int iElementsPerRow = getResources().getInteger(R.integer.list_maindash_column_count);

        // Configure recycler view with a grid layout
        llm_linear = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        uiBinding.statusMainListListLayout.mainList.setLayoutManager(llm_linear);

        // Configure adapter for recycler view
        mAdapter = new StatusListAdapter();
        uiBinding.statusMainListListLayout.mainList.setAdapter(mAdapter);
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private void showError() {
        if (uiBinding != null) {
            uiBinding.statusMainListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.statusMainListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.statusMainListErrorLayout.errorLayout.setVisibility(View.VISIBLE);
            uiBinding.statusMainListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
            uiBinding.statusMainListErrorLayout.tvError.setText(getString(R.string.error_in_list));
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.statusMainListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.statusMainListLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
            uiBinding.statusMainListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.statusMainListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Show the fragment info has no elements.
     */
    private void showNoElements() {
        if (uiBinding != null) {
            uiBinding.statusMainListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.statusMainListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.statusMainListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.statusMainListNoElementsLayout.noElementsLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the fragment info with list.
     */
    private void showList() {
        if (uiBinding != null) {
            uiBinding.statusMainListListLayout.listLayout.setVisibility(View.VISIBLE);
            uiBinding.statusMainListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.statusMainListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.statusMainListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    class StatusListAdapter extends RecyclerView.Adapter<MainStatusFragment.StatusListAdapter.StatusListViewHolder> {

        private List<Log> mData;

        public List<Log> getData() {
            return mData;
        }

        public void setData(List<Log> mData) {
            this.mData = mData;
        }

        @Override
        public MainStatusFragment.StatusListAdapter.StatusListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            CardMaindashboardStatusBinding binding = DataBindingUtil.inflate(inflater, R.layout.card_maindashboard_status,
                    parent, false);
            return new MainStatusFragment.StatusListAdapter.StatusListViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final MainStatusFragment.StatusListAdapter.StatusListViewHolder holder, int position) {
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

        class StatusListViewHolder extends RecyclerView.ViewHolder {

            final CardMaindashboardStatusBinding binding;

            StatusListViewHolder(CardMaindashboardStatusBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

    }


}
