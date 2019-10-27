package com.arejas.dashboardofthings.presentation.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsFragment;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * An activity representing a single Sensor detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link SensorListActivity}.
 */
public class SensorDetailsActivity extends AppCompatActivity {

    @Inject
    ViewModelFactory viewModelFactory;
    private Integer sensorId;
    private SensorDetailsViewModel sensorDetailsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_details);

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
            sensorId = getIntent().getIntExtra(SensorDetailsFragment.SENSOR_ID, -1);
            if (sensorId < 0) sensorId = null;
            // Get the viewmodel
            sensorDetailsViewModel = ViewModelProviders.of(this, this.viewModelFactory).get(SensorDetailsViewModel.class);
            sensorDetailsViewModel.setSensorId(sensorId);
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(SensorDetailsFragment.SENSOR_ID,
                    getIntent().getIntExtra(SensorDetailsFragment.SENSOR_ID, -1));
            arguments.putBoolean(SensorDetailsFragment.TWO_PANE, false);
            SensorDetailsFragment fragment = new SensorDetailsFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.sensor_detail_container, fragment)
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
            navigateUpTo(new Intent(this, SensorListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
