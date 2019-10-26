package com.arejas.dashboardofthings.presentation.ui.activities;

import android.content.Intent;
import android.os.Bundle;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.lifecycle.ViewModelProviders;

import android.view.MenuItem;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * An activity representing a single Network detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link NetworkListActivity}.
 */
public class NetworkDetailActivity extends AppCompatActivity {

    @Inject
    ViewModelFactory viewModelFactory;
    private Integer networkId;
    private NetworkDetailsViewModel networkDetailsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_detail);

        AndroidInjection.inject(this);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            networkId = getIntent().getIntExtra(NetworkDetailFragment.NETWORK_ID, -1);
            if (networkId < 0) networkId = null;
            // Get the viewmodel
            networkDetailsViewModel = ViewModelProviders.of(this, this.viewModelFactory).get(NetworkDetailsViewModel.class);
            networkDetailsViewModel.setNetworkId(networkId);
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(NetworkDetailFragment.NETWORK_ID,
                    getIntent().getIntExtra(NetworkDetailFragment.NETWORK_ID, -1));
            arguments.putBoolean(NetworkDetailFragment.TWO_PANE, false);
            NetworkDetailFragment fragment = new NetworkDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.network_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, NetworkListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
