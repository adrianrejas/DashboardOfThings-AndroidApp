package com.arejas.dashboardofthings.presentation.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.fragments.ActuatorDetailsFragment;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

/**
 * An activity representing a single Actuator detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ActuatorListActivity}.
 */
public class ActuatorDetailsActivity extends AppCompatActivity {

    @Inject
    ViewModelFactory viewModelFactory;
    private Integer actuatorId;
    private ActuatorDetailsViewModel actuatorDetailsViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actuator_details);

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
            actuatorId = getIntent().getIntExtra(ActuatorDetailsFragment.ACTUATOR_ID, -1);
            if (actuatorId < 0) actuatorId = null;
            // Get the viewmodel
            actuatorDetailsViewModel = ViewModelProviders.of(this, this.viewModelFactory).get(ActuatorDetailsViewModel.class);
            actuatorDetailsViewModel.setActuatorId(actuatorId);
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putInt(ActuatorDetailsFragment.ACTUATOR_ID,
                    getIntent().getIntExtra(ActuatorDetailsFragment.ACTUATOR_ID, -1));
            arguments.putBoolean(ActuatorDetailsFragment.TWO_PANE, false);
            ActuatorDetailsFragment fragment = new ActuatorDetailsFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.actuator_detail_container, fragment)
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
            navigateUpTo(new Intent(this, ActuatorListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
