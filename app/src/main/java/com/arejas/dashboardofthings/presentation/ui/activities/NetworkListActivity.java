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
import com.arejas.dashboardofthings.databinding.ActivityNetworkListBinding;
import com.arejas.dashboardofthings.databinding.ItemNetworkListBinding;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkListViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailsFragment;
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveNetworkDialogFragment;
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
 * An activity representing a list of Networks. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link NetworkDetailsActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class NetworkListActivity extends AppCompatActivity implements View.OnClickListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private NetworkListViewModel networkListViewModel;

    @Inject
    ViewModelFactory viewModelFactory;

    private Menu menu;

    private NavigationView navView;
    private DrawerLayout drawerLayout;

    private LiveData<Resource<List<NetworkExtended>>> currentListShown;
    private GridLayoutManager glm_grid;
    private NetworkListAdapter mAdapter;

    ActivityNetworkListBinding uiBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_network_list);

        /* Inject dependencies*/
        AndroidInjection.inject(this);

        setSupportActionBar(uiBinding.toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.navigation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.network_detail_container) != null) {
            mTwoPane = true;
        }

        /* Get view model*/
        networkListViewModel = ViewModelProviders.of(this, this.viewModelFactory).get(NetworkListViewModel.class);

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
                            case R.id.main_navigation_sensors:
                                intent = new Intent(getApplicationContext(),
                                        SensorListActivity.class);
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
                            case R.id.main_navigation_networks:
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
        uiBinding.networkContainer.srlRefreshLayout.setOnRefreshListener(() -> setList(false, true));

        uiBinding.setListener(this);

        // Load network list
        setList(true, false);
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
        currentListShown = networkListViewModel.getListOfNetworks(refreshData);
        if (currentListShown != null) {
            currentListShown.observe(this, listResource -> {
                if (listResource == null) {
                    showError();
                } else {
                    if (listResource.getStatus() == Resource.Status.ERROR) {
                        showError();
                        uiBinding.networkContainer.srlRefreshLayout.setRefreshing(false);
                    } else if (listResource.getStatus() == Resource.Status.LOADING) {
                        if (showLoading)
                            showLoading();
                    } else {
                        updateList(listResource.getData());
                        uiBinding.networkContainer.srlRefreshLayout.setRefreshing(false);
                    }
                }
            });
        }
    }

    private void updateList(List<NetworkExtended> newList) {
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
        uiBinding.networkContainer.networksListListLayout.mainList.setLayoutManager(glm_grid);

        // Configure adapter for recycler view
        mAdapter = new NetworkListAdapter(this, networkListViewModel, mTwoPane);
        uiBinding.networkContainer.networksListListLayout.mainList.setAdapter(mAdapter);
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private void showError() {
        if (uiBinding != null) {
            uiBinding.networkContainer.networksListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.networkContainer.networksListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.networkContainer.networksListErrorLayout.errorLayout.setVisibility(View.VISIBLE);
            uiBinding.networkContainer.networksListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
            uiBinding.networkContainer.networksListErrorLayout.tvError.setText(getString(R.string.error_in_list));
        }
    }

    /**
     * Show the activity info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.networkContainer.networksListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.networkContainer.networksListLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
            uiBinding.networkContainer.networksListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.networkContainer.networksListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Show the activity info has no elements.
     */
    private void showNoElements() {
        if (uiBinding != null) {
            uiBinding.networkContainer.networksListListLayout.listLayout.setVisibility(View.GONE);
            uiBinding.networkContainer.networksListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.networkContainer.networksListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.networkContainer.networksListNoElementsLayout.noElementsLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the activity info with list.
     */
    private void showList() {
        if (uiBinding != null) {
            uiBinding.networkContainer.networksListListLayout.listLayout.setVisibility(View.VISIBLE);
            uiBinding.networkContainer.networksListLoadingLayout.loadingLayout.setVisibility(View.GONE);
            uiBinding.networkContainer.networksListErrorLayout.errorLayout.setVisibility(View.GONE);
            uiBinding.networkContainer.networksListNoElementsLayout.noElementsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, NetworkAddEditActivity.class);
        startActivity(intent);
    }

    public static class NetworkListAdapter
            extends RecyclerView.Adapter<NetworkListAdapter.NetworkListViewHolder> {

        private final NetworkListViewModel mViewModel;
        private final NetworkListActivity mParentActivity;
        public List<NetworkExtended> mData;
        private final boolean mTwoPane;

        public List<NetworkExtended> getData() {
            return mData;
        }

        public void setData(List<NetworkExtended> mData) {
            this.mData = mData;
        }

        private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkExtended item = (NetworkExtended) view.getTag();
                if (mTwoPane) {
                    Bundle arguments = new Bundle();
                    arguments.putInt(NetworkDetailsFragment.NETWORK_ID, item.getId());
                    arguments.putBoolean(NetworkDetailsFragment.TWO_PANE, true);
                    NetworkDetailsFragment fragment = new NetworkDetailsFragment();
                    fragment.setArguments(arguments);
                    mParentActivity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.network_detail_container, fragment)
                            .commit();
                } else {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, NetworkDetailsActivity.class);
                    intent.putExtra(NetworkDetailsFragment.NETWORK_ID, item.getId());

                    context.startActivity(intent);
                }
            }
        };

        NetworkListAdapter(NetworkListActivity parent, NetworkListViewModel viewModel,
                           boolean twoPane) {
            mParentActivity = parent;
            mTwoPane = twoPane;
            mViewModel = viewModel;
        }

        @Override
        public NetworkListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemNetworkListBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_network_list,
                    parent, false);
            return new NetworkListViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final NetworkListViewHolder holder, int position) {
            if ((mData != null) && mData.size() > position) {
                NetworkExtended network = mData.get(position);
                holder.setNetwork(network);
                holder.binding.setNetwork(network);
                holder.binding.setPresenter(holder);
                holder.itemView.setTag(network);
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

        class NetworkListViewHolder extends RecyclerView.ViewHolder implements NetworkElementOptionsListener {

            private Network network;

            final ItemNetworkListBinding binding;

            NetworkListViewHolder(ItemNetworkListBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public Network getNetwork() {
                return network;
            }

            public void setNetwork(Network network) {
                this.network = network;
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
                                        NetworkAddEditActivity.class);
                                intent.putExtra(NetworkAddEditActivity.NETWORK_ID, network.getId());
                                mParentActivity.startActivity(intent);
                                break;
                            case R.id.menu_remove:
                                RemoveNetworkDialogFragment dialog =
                                        new RemoveNetworkDialogFragment(network, mViewModel);
                                dialog.show(mParentActivity.getSupportFragmentManager(), "removeNetwork");
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

    public interface NetworkElementOptionsListener {

        public void optionsClicked (View view);

    }
}
