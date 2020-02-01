package com.arejas.dashboardofthings.presentation.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.fragments.ActuatorDetailsFragment

import javax.inject.Inject

import dagger.android.AndroidInjection

/**
 * An activity representing a single Actuator detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [ActuatorListActivity].
 */
class ActuatorDetailsActivity : AppCompatActivity() {

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null
    private var actuatorId: Int? = null
    private var actuatorDetailsViewModel: ActuatorDetailsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actuator_details)

        AndroidInjection.inject(this)

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
            actuatorId = intent.getIntExtra(ActuatorDetailsFragment.ACTUATOR_ID, -1)
            actuatorId?.compareTo(0)?.equals(-1).let { actuatorId = null }
            // Get the viewmodel
            actuatorDetailsViewModel = ViewModelProviders.of(this, this.viewModelFactory)
                .get(ActuatorDetailsViewModel::class.java)
            actuatorDetailsViewModel!!.actuatorId = actuatorId
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            val arguments = Bundle()
            arguments.putInt(
                ActuatorDetailsFragment.ACTUATOR_ID,
                intent.getIntExtra(ActuatorDetailsFragment.ACTUATOR_ID, -1)
            )
            arguments.putBoolean(ActuatorDetailsFragment.TWO_PANE, false)
            val fragment = ActuatorDetailsFragment()
            fragment.arguments = arguments
            supportFragmentManager.beginTransaction()
                .add(R.id.actuator_detail_container, fragment)
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(Intent(this, ActuatorListActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
