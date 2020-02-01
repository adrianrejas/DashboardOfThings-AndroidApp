package com.arejas.dashboardofthings.presentation.ui.activities

import android.content.Intent
import android.os.Bundle

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailsFragment

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders

import android.view.MenuItem

import javax.inject.Inject

import dagger.android.AndroidInjection

/**
 * An activity representing a single Network detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a [NetworkListActivity].
 */
class NetworkDetailsActivity : AppCompatActivity() {

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null
    private var networkId: Int? = null
    private var networkDetailsViewModel: NetworkDetailsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_network_details)

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
            networkId = intent.getIntExtra(NetworkDetailsFragment.NETWORK_ID, -1)
            if (networkId < 0) networkId = null
            // Get the viewmodel
            networkDetailsViewModel = ViewModelProviders.of(this, this.viewModelFactory)
                .get(NetworkDetailsViewModel::class.java)
            networkDetailsViewModel!!.networkId = networkId
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            val arguments = Bundle()
            arguments.putInt(
                NetworkDetailsFragment.NETWORK_ID,
                intent.getIntExtra(NetworkDetailsFragment.NETWORK_ID, -1)
            )
            arguments.putBoolean(NetworkDetailsFragment.TWO_PANE, false)
            val fragment = NetworkDetailsFragment()
            fragment.arguments = arguments
            supportFragmentManager.beginTransaction()
                .add(R.id.network_detail_container, fragment)
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
            navigateUpTo(Intent(this, NetworkListActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
