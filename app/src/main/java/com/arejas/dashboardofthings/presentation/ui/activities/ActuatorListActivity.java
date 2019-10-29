package com.arejas.dashboardofthings.presentation.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.ActivityActuatorListBinding;
import com.arejas.dashboardofthings.databinding.ItemActuatorListBinding;
import com.arejas.dashboardofthings.domain.entities.database.Actuator;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorListViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.fragments.ActuatorDetailsFragment;
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveActuatorDialogFragment;
import com.arejas.dashboardofthings.utils.Utils;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class ActuatorListActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private ActuatorListViewModel actuatorListViewModel;

    @Inject
    ViewModelFactory viewModelFactory;

    private Menu menu;

    private NavigationView navView;
    private DrawerLayout drawerLayout;

    private LiveData<Resource<List<ActuatorExtended>>> currentListShown;
    private GridLayoutManager glm_grid;
    private ActuatorListActivity.ActuatorListAdapter mAdapter;

    ActivityActuatorListBinding uiBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_actuator_list);

        /* Inject dependencies*/
        AndroidInjection.inject(this);

        setSupportActionBar(uiBinding.toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.navigation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.actuator_detail_container) != null) {
            mTwoPane = true;
        }

        /* Get view model*/
        actuatorListViewModel = ViewModelProviders.of(this, this.viewModelFactory).get(ActuatorListViewModel.class);

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
                            case R.id.main_navigation_sensors:
                                intent = new Intent(getApplicationContext(),
                                        SensorListActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                                break;
                            case R.id.main_navigation_actuators:
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
        uiBinding.actuatorContainer.srlRefreshLayout.setOnRefreshListener(() -> setList(false, true));

        uiBinding.setListener(this);

        // Load actuator list
        setList(true, false);

        // If actuator ID passed at the beginning and in two panel mode, load the actuator in the details area
        if ((getIntent() != null) && (getIntent().getExtras() != null) &&
                (getIntent().getExtras().containsKey(ActuatorDetailsFragment.ACTUATOR_ID))) {
            int actuatorIdToLoadAtInit = getIntent().getIntExtra(ActuatorDetailsFragment.ACTUATOR_ID, -1);
            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putInt(ActuatorDetailsFragment.ACTUATOR_ID, actuatorIdToLoadAtInit);
                arguments.putBoolean(ActuatorDetailsFragment.TWO_PANE, true);
                ActuatorDetailsFragment fragment = new ActuatorDetailsFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.actuator_detail_container, fragment)
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
                Utils.stopControlService();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        currentListShown = actuatorListViewModel.getListOfActuators(refreshData);
        if (currentListShown != null) {
            currentListShown.observe(this, listResource -> {
                if (listResource == null) {
                    showError();
                } else {
                    if (listResource.getStatus() == Resource.Status.ERROR) {
                        showError();
                        uiBinding.actuatorContainer.srlRefreshLayout.setRefreshing(false);
                    } else if (listResource.getStatus() == Resource.Status.LOADING) {
                        if (showLoading)
                            showLoading();
                    } else {
                        updateList(listResource.getData());
                        uiBinding.actuatorContainer.srlRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    private void updateList(List<ActuatorExtended> newList) {
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
        uiBinding.actuatorContainer.actuatorsListListLayout.mainList.setLayoutManager(glm_grid);

        // Configure adapter for recycler view
        mAdapter = new ActuatorListActivity.ActuatorListAdapter(this, actuatorListViewModel, mTwoPane);
        uiBinding.actuatorContainer.actuatorsListListLayout.mainList.setAdapter(mAdapter);
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private void showError() {
        if (uiBinding != null) {
            uiBinding.actuatorContainer.actuatorsListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.actuatorContainer.actuatorsListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.actuatorContainer.actuatorsListErrorLayout.errorLayout.setVisibility(View.VISIBLE);
            uiBinding.actuatorContainer.actuatorsListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
            uiBinding.actuatorContainer.actuatorsListErrorLayout.tvError.setText(getString(R.string.error_in_list));
        }
    }

    /**
     * Show the activity info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.actuatorContainer.actuatorsListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.actuatorContainer.actuatorsListLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
            uiBinding.actuatorContainer.actuatorsListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.actuatorContainer.actuatorsListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Show the activity info has no elements.
     */
    private void showNoElements() {
        if (uiBinding != null) {
            uiBinding.actuatorContainer.actuatorsListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.actuatorContainer.actuatorsListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.actuatorContainer.actuatorsListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.actuatorContainer.actuatorsListNoElementsLayout.noElementsLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the activity info with list.
     */
    private void showList() {
        if (uiBinding != null) {
            uiBinding.actuatorContainer.actuatorsListListLayout.listLayout.setVisibility(View.VISIBLE);
            uiBinding.actuatorContainer.actuatorsListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.actuatorContainer.actuatorsListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.actuatorContainer.actuatorsListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(DotApplication.getContext(),
                ActuatorAddEditActivity.class);
        DotApplication.getContext().startActivity(intent);
    }

    public static class ActuatorListAdapter
            extends RecyclerView.Adapter<ActuatorListActivity.ActuatorListAdapter.ActuatorListViewHolder> {

        private final ActuatorListViewModel mViewModel;
        private final ActuatorListActivity mParentActivity;
        public List<ActuatorExtended> mData;
        private final boolean mTwoPane;

        public List<ActuatorExtended> getData() {
            return mData;
        }

        public void setData(List<ActuatorExtended> mData) {
            this.mData = mData;
        }

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActuatorExtended item = (ActuatorExtended) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putInt(ActuatorDetailsFragment.ACTUATOR_ID, item.getId());
                    arguments.putBoolean(ActuatorDetailsFragment.TWO_PANE, true);
                    ActuatorDetailsFragment fragment = new ActuatorDetailsFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.actuator_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, ActuatorDetailsActivity.class);
                    intent.putExtra(ActuatorDetailsFragment.ACTUATOR_ID, item.getId());
                    context.startActivity(intent);
                }
            }
        };

        ActuatorListAdapter(ActuatorListActivity parent, ActuatorListViewModel viewModel,
                          boolean twoPane) {
            mParentActivity = parent;
            mTwoPane = twoPane;
            mViewModel = viewModel;
        }

        @Override
        public ActuatorListActivity.ActuatorListAdapter.ActuatorListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemActuatorListBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_actuator_list,
                    parent, false);
            return new ActuatorListActivity.ActuatorListAdapter.ActuatorListViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final ActuatorListActivity.ActuatorListAdapter.ActuatorListViewHolder holder, int position) {
            if ((mData != null) && mData.size() > position) {
                ActuatorExtended actuator = mData.get(position);
                holder.setActuator(actuator);
                holder.binding.setActuator(actuator);
                holder.binding.setPresenter(holder);
                holder.itemView.setTag(actuator);
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

        class ActuatorListViewHolder extends RecyclerView.ViewHolder implements ActuatorListActivity.ActuatorElementOptionsListener {

            private Actuator actuator;

            final ItemActuatorListBinding binding;

            ActuatorListViewHolder(ItemActuatorListBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public Actuator getActuator() {
                return actuator;
            }

            public void setActuator(Actuator actuator) {
                this.actuator = actuator;
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
                                Intent intent = new Intent(DotApplication.getContext(),
                                        ActuatorAddEditActivity.class);
                                intent.putExtra(ActuatorAddEditActivity.ACTUATOR_ID, actuator.getId());
                                DotApplication.getContext().startActivity(intent);
                                break;
                            case R.id.menu_remove:
                                RemoveActuatorDialogFragment dialog =
                                        new RemoveActuatorDialogFragment(actuator, mViewModel);
                                dialog.show(mParentActivity.getSupportFragmentManager(), "removeActuator");
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

    public interface ActuatorElementOptionsListener {

        public void optionsClicked (View view);

    }
    
}
