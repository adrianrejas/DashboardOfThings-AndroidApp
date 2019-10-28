package com.arejas.dashboardofthings.presentation.ui.widget;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.ActivitySelectSensorForWidgetBinding;
import com.arejas.dashboardofthings.databinding.ItemSensorListBinding;
import com.arejas.dashboardofthings.databinding.ItemSensorSelectWidgetBinding;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorListViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkListActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.SensorAddEditActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.SensorDetailsActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.SensorListActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.SettingsActivity;
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsFragment;
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveSensorDialogFragment;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class SelectSensorForWidgetActivity extends AppCompatActivity {

    public static final String WIDGET_ID = "widget_id";

    @Inject
    ViewModelFactory viewModelFactory;

    private SensorListViewModel sensorListViewModel;

    ActivitySelectSensorForWidgetBinding uiBinding;
    private SensorListAdapter mAdapter;
    private GridLayoutManager glm_grid;
    private LiveData<Resource<List<SensorExtended>>> currentListShown;
    private Integer widgetIdToConfigure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_sensor_for_widget);
        
        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_sensor_for_widget);
        
        /* Inject dependencies*/
        AndroidInjection.inject(this);

        // If sensor ID passed at the beginning and in two panel mode, load the sensor in the details area
        if ((savedInstanceState != null) && (savedInstanceState.containsKey(WIDGET_ID))) {
            widgetIdToConfigure = savedInstanceState.getInt(WIDGET_ID, -1);
            if (widgetIdToConfigure < 0) widgetIdToConfigure = null;
        } else if ((getIntent() != null) && (getIntent().getExtras() != null) &&
                (getIntent().getExtras().containsKey(WIDGET_ID))) {
            widgetIdToConfigure = getIntent().getIntExtra(WIDGET_ID, -1);
            if (widgetIdToConfigure < 0) widgetIdToConfigure = null;
        }
        if (widgetIdToConfigure == null) finish();

        setSupportActionBar(uiBinding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        
        /* Get view model*/
        sensorListViewModel = ViewModelProviders.of(this, this.viewModelFactory).get(SensorListViewModel.class);

        // Configure adapter for recycler view
        configureListAdapter();

        // Load sensor list
        setList();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(WIDGET_ID, widgetIdToConfigure);
        super.onSaveInstanceState(outState);
    }

    /**
     * Function called for setting a new sensor list
     *
     */
    private void setList() {
        showLoading();
        if (currentListShown != null) {
            currentListShown.removeObservers(this);
            currentListShown = null;
        }
        currentListShown = sensorListViewModel.getListOfSensors(true);
        if (currentListShown != null) {
            currentListShown.observe(this, listResource -> {
                if (listResource == null) {
                    showError();
                } else {
                    if (listResource.getStatus() == Resource.Status.ERROR) {
                        showError();
                    } else if (listResource.getStatus() == Resource.Status.LOADING) {
                        showLoading();
                    } else {
                        updateList(listResource.getData());
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

        // Configure recycler view with a grid layout
        glm_grid = new GridLayoutManager(getApplicationContext(), 1);
        uiBinding.selectSensorWidgetListLayout.mainList.setLayoutManager(glm_grid);

        // Configure adapter for recycler view
        mAdapter = new SensorListAdapter(this, widgetIdToConfigure);
        uiBinding.selectSensorWidgetListLayout.mainList.setAdapter(mAdapter);
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private void showError() {
        if (uiBinding != null) {
            uiBinding.selectSensorWidgetListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.selectSensorWidgetLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.selectSensorWidgetErrorLayout.errorLayout.setVisibility(View.VISIBLE);
            uiBinding.selectSensorWidgetNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
            uiBinding.selectSensorWidgetErrorLayout.tvError.setText(getString(R.string.error_in_list));
        }
    }

    /**
     * Show the activity info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.selectSensorWidgetListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.selectSensorWidgetLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
            uiBinding.selectSensorWidgetErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.selectSensorWidgetNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Show the activity info has no elements.
     */
    private void showNoElements() {
        if (uiBinding != null) {
            uiBinding.selectSensorWidgetListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.selectSensorWidgetLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.selectSensorWidgetErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.selectSensorWidgetNoElementsLayout.noElementsLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the activity info with list.
     */
    private void showList() {
        if (uiBinding != null) {
            uiBinding.selectSensorWidgetListLayout.listLayout.setVisibility(View.VISIBLE);
            uiBinding.selectSensorWidgetLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.selectSensorWidgetErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.selectSensorWidgetNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    public static class SensorListAdapter
            extends RecyclerView.Adapter<SensorListAdapter.SensorListViewHolder> {

        private final Activity mParentActivity;
        public List<SensorExtended> mData;
        private final Integer widgetId;

        public List<SensorExtended> getData() {
            return mData;
        }

        public void setData(List<SensorExtended> mData) {
            this.mData = mData;
        }

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SensorExtended item = (SensorExtended) view.getTag();
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mParentActivity);
                SensorWidgetService.startActionSetRecipeForWidget(mParentActivity.getApplicationContext(), widgetId, item.getId());
                mParentActivity.finish();
            }
        };

        SensorListAdapter(Activity parent, Integer widgetId) {
            mParentActivity = parent;
            this.widgetId = widgetId;
        }

        @Override
        public SensorListAdapter.SensorListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemSensorSelectWidgetBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_sensor_select_widget,
                    parent, false);
            return new SensorListViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final SensorListViewHolder holder, int position) {
            if ((mData != null) && mData.size() > position) {
                SensorExtended sensor = mData.get(position);
                holder.setSensor(sensor);
                holder.binding.setSensor(sensor);
                holder.itemView.setTag(sensor);
                holder.itemView.setOnClickListener(mOnClickListener);
            }
        }

        @Override
        public int getItemCount() {
            if (mData != null)
                return mData.size();
            else
                return 0;
        }

        class SensorListViewHolder extends RecyclerView.ViewHolder {

            private Sensor sensor;

            final ItemSensorSelectWidgetBinding binding;

            SensorListViewHolder(ItemSensorSelectWidgetBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public Sensor getSensor() {
                return sensor;
            }

            public void setSensor(Sensor sensor) {
                this.sensor = sensor;
            }

        }
    }
    
}
