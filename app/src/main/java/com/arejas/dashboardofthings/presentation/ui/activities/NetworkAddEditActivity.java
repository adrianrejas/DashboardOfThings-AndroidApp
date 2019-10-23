package com.arejas.dashboardofthings.presentation.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.ActivityNetworkAddEditBinding;
import com.arejas.dashboardofthings.domain.entities.database.Network;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkListViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailFragment;
import com.arejas.dashboardofthings.presentation.ui.helpers.AddEditElementPresenter;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * An activity representing a single Network detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link NetworkListActivity}.
 */
public class NetworkAddEditActivity extends AppCompatActivity implements AddEditElementPresenter {

    public static final String NETWORK_ID = "network_id";

    private NetworkDetailsViewModel networkDetailsViewModel;

    ActivityNetworkAddEditBinding uiBinding;

    private Menu menu;

    @Inject
    ViewModelFactory viewModelFactory;

    
    boolean editionMode;
    Integer networkId;
    private Network networkObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_detail);

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_network_add_edit);

        /* Inject dependencies*/
        AndroidInjection.inject(this);

        setSupportActionBar(uiBinding.toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.navigation);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        if (getIntent().getExtras().containsKey(NETWORK_ID)) {
            networkId = getIntent().getIntExtra(NETWORK_ID, -1);
        } else {
            networkId = null;
        }
        if ((networkId != null) || (networkId < 0)) {
            editionMode = false;
        } else {
            editionMode = true;
        }

        uiBinding.setPresenter(this);
        uiBinding.setEditionMode(editionMode);

        /* Get view model*/
        this.viewModelFactory.setNetworkIdToLoad(networkId);
        networkDetailsViewModel = ViewModelProviders.of(this, this.viewModelFactory).get(NetworkDetailsViewModel.class);

        if (editionMode) {
            uiBinding.toolbar.setTitle(getString(R.string.toolbar_title_edit_network));
            networkDetailsViewModel.getNetwork(true).observe(this, networkExtendedResource -> {
                if (networkExtendedResource == null) {
                    ToastHelper.showToast(getString(R.string.toast_edition_failed));
                    finish();
                } else {
                    if (networkExtendedResource.getStatus() == Resource.Status.ERROR) {
                        ToastHelper.showToast(getString(R.string.toast_edition_failed));
                        finish();
                    } else if (networkExtendedResource.getStatus() == Resource.Status.LOADING) {
                        showLoading();
                    } else {
                        NetworkExtended network = networkExtendedResource.getData();
                        if (network != null) {
                            networkObject = network;
                            uiBinding.setNetwork(network);
                            uiBinding.toolbar.setTitle(network.getName());
                            showEditArea();
                        } else {
                            ToastHelper.showToast(getString(R.string.toast_edition_failed));
                            finish();
                        }
                    }
                }
            });
        } else {
            uiBinding.toolbar.setTitle(getString(R.string.toolbar_title_new_network));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_addeditelement, menu);
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
                finish();
                return true;
            case R.id.menu_ok:
                //TODO CREATION FILE
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Show the activity info is loading.
     */
    private void showLoading() {
        if (uiBinding != null) {
            uiBinding.networkAddeditEditareaLayout.setVisibility(View.GONE);
            uiBinding.networkAddeditLoadingLayout.loadingLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show the edition area.
     */
    private void showEditArea() {
        if (uiBinding != null) {
            uiBinding.networkAddeditEditareaLayout.setVisibility(View.VISIBLE);
            uiBinding.networkAddeditLoadingLayout.loadingLayout.setVisibility(View.GONE);
        }
    }
    
}
