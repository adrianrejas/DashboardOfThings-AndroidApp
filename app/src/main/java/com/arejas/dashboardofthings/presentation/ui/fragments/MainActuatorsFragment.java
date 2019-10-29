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
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.CardMaindashboardActuatorBinding;
import com.arejas.dashboardofthings.databinding.FragmentMainactuatorsBinding;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.database.DataValue;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MainDashboardViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class MainActuatorsFragment extends Fragment {

    FragmentMainactuatorsBinding uiBinding;

    @Inject
    ViewModelFactory viewModelFactory;

    private MainDashboardViewModel mainnetwork_addeditViewModel;

    private LiveData<Resource<List<ActuatorExtended>>> currentListShown;
    private LiveData<Resource<List<DataValue>>> dataValuesReceivedManaged;
    private StaggeredGridLayoutManager glm_grid;
    private MainActuatorsFragment.ActuatorsListAdapter mAdapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Inject dependencies
        AndroidSupportInjection.inject(this);
        // Get the main dashboard activity view model and observe the changes in the details
        mainnetwork_addeditViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity()), viewModelFactory).get(MainDashboardViewModel.class);
        setList(true, false);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        uiBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_mainactuators, container, false);
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
        currentListShown = mainnetwork_addeditViewModel.getListOfActuatorsMainDashboard(refreshData);
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
    }

    private void updateList(List<ActuatorExtended> newList) {
        if ((newList != null) && (!newList.isEmpty())) {
            showList();
            mAdapter.setActuators(newList);
            mAdapter.notifyDataSetChanged();
        } else {
            showNoElements();
        }
    }

    private void configureListAdapter() {

        /* Get number of items in a row*/
        int iElementsPerRow = getResources().getInteger(R.integer.list_maindash_column_count);

        // Configure recycler view with a grid layout
        glm_grid = new StaggeredGridLayoutManager(iElementsPerRow, StaggeredGridLayoutManager.VERTICAL);
        uiBinding.actuatorsMainListListLayout.mainList.setLayoutManager(glm_grid);

        // Configure adapter for recycler view
        mAdapter = new MainActuatorsFragment.ActuatorsListAdapter();
        uiBinding.actuatorsMainListListLayout.mainList.setAdapter(mAdapter);
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private void showError() {
        if (uiBinding != null) {
            uiBinding.actuatorsMainListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.actuatorsMainListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.actuatorsMainListErrorLayout.errorLayout.setVisibility(View.VISIBLE);
            uiBinding.actuatorsMainListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
            uiBinding.actuatorsMainListErrorLayout.tvError.setText(getString(R.string.error_in_list));
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.actuatorsMainListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.actuatorsMainListLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
            uiBinding.actuatorsMainListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.actuatorsMainListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Show the fragment info has no elements.
     */
    private void showNoElements() {
        if (uiBinding != null) {
            uiBinding.actuatorsMainListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.actuatorsMainListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.actuatorsMainListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.actuatorsMainListNoElementsLayout.noElementsLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the fragment info with list.
     */
    private void showList() {
        if (uiBinding != null) {
            uiBinding.actuatorsMainListListLayout.listLayout.setVisibility(View.VISIBLE);
            uiBinding.actuatorsMainListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.actuatorsMainListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.actuatorsMainListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    class ActuatorsListAdapter extends RecyclerView.Adapter<MainActuatorsFragment.ActuatorsListAdapter.ActuatorListViewHolder> {

        private List<ActuatorExtended> mActuators;

        public List<ActuatorExtended> getActuators() {
            return mActuators;
        }

        public void setActuators(List<ActuatorExtended> mData) {
            this.mActuators = mData;
        }

        @Override
        public MainActuatorsFragment.ActuatorsListAdapter.ActuatorListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            CardMaindashboardActuatorBinding binding = DataBindingUtil.inflate(inflater, R.layout.card_maindashboard_actuator,
                    parent, false);
            return new MainActuatorsFragment.ActuatorsListAdapter.ActuatorListViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final MainActuatorsFragment.ActuatorsListAdapter.ActuatorListViewHolder holder, int position) {
            if ((mActuators != null) && (mActuators.size() > position)) {
                holder.itemView.setTag(position);
                ActuatorExtended actuator = mActuators.get(position);
                if (actuator != null) {
                    holder.setActuator(actuator);
                    holder.binding.setPresenter(holder);
                    holder.binding.setActuator(actuator);
                }
            }
        }

        @Override
        public int getItemCount() {
            if (mActuators != null) {
                return mActuators.size();
            } else {
                return 0;
            }
        }

        class ActuatorListViewHolder extends RecyclerView.ViewHolder implements ActuatorMainDashboardListener {

            Actuator actuator;

            final CardMaindashboardActuatorBinding binding;

            ActuatorListViewHolder(CardMaindashboardActuatorBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void setActuator(Actuator actuator) {
                this.actuator = actuator;
            }

            public Actuator getActuator() {
                return actuator;
            }

            @Override
            public void sendInteger() {
                try {
                    if (actuator != null) {
                        String data = binding.etActuatorIntegerValue.getText().toString();
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
                            mainnetwork_addeditViewModel.sendActuatorData(actuator, data);
                    }
                } catch (Exception e) {
                    ToastHelper.showToast(getString(R.string.set_toast_error));
                }
            }

            @Override
            public void sendFloat() {
                try {
                    if (actuator != null) {
                        String data = binding.etActuatorDecimalValue.getText().toString();
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
                            mainnetwork_addeditViewModel.sendActuatorData(actuator, data);
                    }
                } catch (Exception e) {
                    ToastHelper.showToast(getString(R.string.set_toast_error));
                }
            }

            @Override
            public void sendBooleanFalse() {
                if (actuator != null) {
                    mainnetwork_addeditViewModel.sendActuatorData(actuator, "false");
                }
            }

            @Override
            public void sendBooleanTrue() {
                if (actuator != null) {
                    mainnetwork_addeditViewModel.sendActuatorData(actuator, "true");
                }
            }

            @Override
            public void sendString() {
                if (actuator != null) {
                    String data = binding.etActuatorStringValue.getText().toString();
                    if (data != null)
                        mainnetwork_addeditViewModel.sendActuatorData(actuator, data);
                }
            }
        }

    }

    public interface ActuatorMainDashboardListener {

        public void sendInteger ();

        public void sendFloat ();

        public void sendBooleanFalse ();

        public void sendBooleanTrue ();

        public void sendString ();

    }
}
