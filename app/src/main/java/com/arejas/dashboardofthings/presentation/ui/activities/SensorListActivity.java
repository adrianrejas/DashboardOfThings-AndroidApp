package com.arejas.dashboardofthings.presentation.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.ActivitySensorListBinding;
import com.arejas.dashboardofthings.databinding.ItemSensorListBinding;
import com.arejas.dashboardofthings.domain.entities.database.Sensor;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorListViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsFragment;
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveSensorDialogFragment;
import com.arejas.dashboardofthings.utils.Utils;
import com.google.android.material.navigation.NavigationView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * An activity representing a list of Sensors. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SensorDetailsActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class SensorListActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private SensorListViewModel sensorListViewModel;

    @Inject
    ViewModelFactory viewModelFactory;

    private Menu menu;

    private NavigationView navView;
    private DrawerLayout drawerLayout;

    private LiveData<Resource<List<SensorExtended>>> currentListShown;
    private GridLayoutManager glm_grid;
    private SensorListAdapter mAdapter;

    ActivitySensorListBinding uiBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_sensor_list);

        /* Inject dependencies*/
        AndroidInjection.inject(this);

        setSupportActionBar(uiBinding.toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.navigation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.sensor_detail_container) != null) {
            mTwoPane = true;
        }

        /* Get view model*/
        sensorListViewModel = ViewModelProviders.of(this, this.viewModelFactory).get(SensorListViewModel.class);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navView = (NavigationView)findViewById(R.id.navigation_view);
        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        boolean fragmentTransaction = false;
                        Fragment fragment = null;

                        Intent intent = null;
                        switch (menuItem.getItemId()) {
                            case R.id.main_navigation_dashboard:
                                intent = new Intent(getApplicationContext(),
                                        MainDashboardActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                break;
                            case R.id.main_navigation_networks:
                                intent = new Intent(getApplicationContext(),
                                        NetworkListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                break;
                            case R.id.main_navigation_actuators:
                                intent = new Intent(getApplicationContext(),
                                        ActuatorListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                break;
                            case R.id.main_navigation_sensors:
                                break;
                            case R.id.main_navigation_map:
                                intent = new Intent(getApplicationContext(),
                                        MapActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                break;
                        }
                        drawerLayout.closeDrawers();
                        return true;
                    }
                });

        // Configure adapter for recycler view
        configureListAdapter();

        // Set action of refreshing list when refreshing gesture detected
        uiBinding.sensorContainer.srlRefreshLayout.setOnRefreshListener(() -> setList(false, true));

        uiBinding.setListener(this);

        // Load sensor list
        setList(true, false);

        // If sensor ID passed at the beginning and in two panel mode, load the sensor in the details area
        if ((getIntent() != null) && (getIntent().getExtras() != null) &&
                (getIntent().getExtras().containsKey(SensorDetailsFragment.SENSOR_ID))) {
            int sensorIdToLoadAtInit = getIntent().getIntExtra(SensorDetailsFragment.SENSOR_ID, -1);
            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putInt(SensorDetailsFragment.SENSOR_ID, sensorIdToLoadAtInit);
                arguments.putBoolean(SensorDetailsFragment.TWO_PANE, true);
                SensorDetailsFragment fragment = new SensorDetailsFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.sensor_detail_container, fragment)
                        .commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mainactivites, menu);
        this.menu = menu;
        return true;
    }

    /**
     * Function called when a menu item is selected.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_options:
                startActivity(new Intent(getApplicationContext(),
                        SettingsActivity.class));
                return true;
            case R.id.menu_shutdown_app:
                Utils.stopControlService(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        currentListShown = sensorListViewModel.getListOfSensors(refreshData);
        if (currentListShown != null) {
            currentListShown.observe(this, listResource -> {
                if (listResource == null) {
                    showError();
                } else {
                    if (listResource.getStatus() == Resource.Status.ERROR) {
                        showError();
                        uiBinding.sensorContainer.srlRefreshLayout.setRefreshing(false);
                    } else if (listResource.getStatus() == Resource.Status.LOADING) {
                        if (showLoading)
                            showLoading();
                    } else {
                        updateList(listResource.getData());
                        uiBinding.sensorContainer.srlRefreshLayout.setRefreshing(false);
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
        uiBinding.sensorContainer.sensorsListListLayout.mainList.setLayoutManager(glm_grid);

        // Configure adapter for recycler view
        mAdapter = new SensorListAdapter(this, sensorListViewModel, mTwoPane);
        uiBinding.sensorContainer.sensorsListListLayout.mainList.setAdapter(mAdapter);
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private void showError() {
        if (uiBinding != null) {
            uiBinding.sensorContainer.sensorsListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.sensorContainer.sensorsListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.sensorContainer.sensorsListErrorLayout.errorLayout.setVisibility(View.VISIBLE);
            uiBinding.sensorContainer.sensorsListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
            uiBinding.sensorContainer.sensorsListErrorLayout.tvError.setText(getString(R.string.error_in_list));
        }
    }

    /**
     * Show the activity info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.sensorContainer.sensorsListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.sensorContainer.sensorsListLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
            uiBinding.sensorContainer.sensorsListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.sensorContainer.sensorsListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Show the activity info has no elements.
     */
    private void showNoElements() {
        if (uiBinding != null) {
            uiBinding.sensorContainer.sensorsListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.sensorContainer.sensorsListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.sensorContainer.sensorsListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.sensorContainer.sensorsListNoElementsLayout.noElementsLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the activity info with list.
     */
    private void showList() {
        if (uiBinding != null) {
            uiBinding.sensorContainer.sensorsListListLayout.listLayout.setVisibility(View.VISIBLE);
            uiBinding.sensorContainer.sensorsListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.sensorContainer.sensorsListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.sensorContainer.sensorsListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, SensorAddEditActivity.class);
        startActivity(intent);
    }

    public static class SensorListAdapter
            extends RecyclerView.Adapter<SensorListAdapter.SensorListViewHolder> {

        private final SensorListViewModel mViewModel;
        private final SensorListActivity mParentActivity;
        public List<SensorExtended> mData;
        private final boolean mTwoPane;

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
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putInt(SensorDetailsFragment.SENSOR_ID, item.getId());
                    arguments.putBoolean(SensorDetailsFragment.TWO_PANE, true);
                    SensorDetailsFragment fragment = new SensorDetailsFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.sensor_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, SensorDetailsActivity.class);
                    intent.putExtra(SensorDetailsFragment.SENSOR_ID, item.getId());

                    context.startActivity(intent);
                }
            }
        };

        SensorListAdapter(SensorListActivity parent, SensorListViewModel viewModel,
                           boolean twoPane) {
            mParentActivity = parent;
            mTwoPane = twoPane;
            mViewModel = viewModel;
        }

        @Override
        public SensorListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemSensorListBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_sensor_list,
                    parent, false);
            return new SensorListViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final SensorListViewHolder holder, int position) {
            if ((mData != null) && mData.size() > position) {
                SensorExtended sensor = mData.get(position);
                holder.setSensor(sensor);
                holder.binding.setSensor(sensor);
                holder.binding.setPresenter(holder);
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

        class SensorListViewHolder extends RecyclerView.ViewHolder implements SensorElementOptionsListener {

            private Sensor sensor;

            final ItemSensorListBinding binding;

            SensorListViewHolder(ItemSensorListBinding binding) {
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
            public void optionsClicked(View view) {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(DotApplication.getContext(), view);
                //inflating menu from xml resource
                popup.inflate(R.menu.menu_element_management_item);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_edit:
                                Intent intent = new Intent(mParentActivity.getApplicationContext(),
                                        SensorAddEditActivity.class);
                                intent.putExtra(SensorAddEditActivity.SENSOR_ID, sensor.getId());
                                mParentActivity.startActivity(intent);
                                break;
                            case R.id.menu_remove:
                                RemoveSensorDialogFragment dialog =
                                        new RemoveSensorDialogFragment(sensor, mViewModel);
                                dialog.show(mParentActivity.getSupportFragmentManager(), "removeSensor");
                                break;
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();
            }
        }
    }

    public interface SensorElementOptionsListener {

        public void optionsClicked (View view);

    }
}
