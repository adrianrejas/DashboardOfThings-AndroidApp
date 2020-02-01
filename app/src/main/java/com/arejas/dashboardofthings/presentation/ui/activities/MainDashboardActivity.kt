package com.arejas.dashboardofthings.presentation.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.ActivityMaindashboardBinding
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MainDashboardViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.fragments.MainActuatorsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.MainHistoryFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.MainLogsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.MainSensorsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.MainStatusFragment
import com.arejas.dashboardofthings.utils.Utils
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.material.navigation.NavigationView

import javax.inject.Inject

import dagger.android.AndroidInjection

class MainDashboardActivity : AppCompatActivity() {

    private var mainDashboardViewModel: MainDashboardViewModel? = null

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    private var menu: Menu? = null

    private var navView: NavigationView? = null
    private var drawerLayout: DrawerLayout? = null

    internal var uiBinding: ActivityMaindashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_maindashboard)

        /* Inject dependencies*/
        AndroidInjection.inject(this)

        // Configure toolbar
        setSupportActionBar(uiBinding.toolbar)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.navigation)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setTitle(R.string.title_activity_maindash)

        // Starts control service if not initiated
        Utils.startControlService(this)

        /* Get view model*/
        mainDashboardViewModel = ViewModelProviders.of(this, this.viewModelFactory)
            .get(MainDashboardViewModel::class.java)

        drawerLayout = findViewById<View>(R.id.drawer_layout)
        navView = findViewById<View>(R.id.navigation_view)
        navView!!.setNavigationItemSelectedListener { menuItem ->
            val fragmentTransaction = false
            val fragment: Fragment? = null

            var intent: Intent? = null
            when (menuItem.itemId) {
                R.id.main_navigation_dashboard -> {
                }
                R.id.main_navigation_sensors -> {
                    intent = Intent(
                        applicationContext,
                        SensorListActivity::class.java
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                R.id.main_navigation_actuators -> {
                    intent = Intent(
                        applicationContext,
                        ActuatorListActivity::class.java
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                R.id.main_navigation_networks -> {
                    intent = Intent(
                        applicationContext,
                        NetworkListActivity::class.java
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                R.id.main_navigation_map -> {
                    intent = Intent(
                        applicationContext,
                        MapActivity::class.java
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout!!.closeDrawers()
            true
        }

        // Init the view pager with a fragment adapter for showing the fragments with different info
        // of the main dashboard in a tab system
        val fragmentAdapter = MainDashboardFragmentAdapter(supportFragmentManager, this)
        uiBinding.vpTabViewerMaindashboard.adapter = fragmentAdapter
        uiBinding.tlTabsMaindashboard.setupWithViewPager(uiBinding.vpTabViewerMaindashboard)

        MobileAds.initialize(this) { Log.d("ADS", "Anuncios cargados") }
        val mAdView = findViewById<View>(R.id.ad_maindashboard) as AdView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_mainactivites, menu)
        this.menu = menu
        return true
    }

    /**
     * Function called when a menu item is selected.
     *
     * @param item
     * @return
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                drawerLayout!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.menu_options -> {
                startActivity(
                    Intent(
                        applicationContext,
                        SettingsActivity::class.java
                    )
                )
                return true
            }
            R.id.menu_shutdown_app -> {
                Utils.stopControlService(this)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This adapter is used for defining the tab system of the main dashboard activity, providing the
     * fragments it will used, so as the tab configuration.
     */
    internal class MainDashboardFragmentAdapter(
        fragmentManager: FragmentManager,
        private val mContext: Context?
    ) : FragmentStatePagerAdapter(
        fragmentManager,
        FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

        // Returns total number of pages
        override fun getCount(): Int {
            return NUM_ITEMS
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment {
            when (position) {
                0 // Details
                -> return MainSensorsFragment()
                1 // Cast
                -> return MainActuatorsFragment()
                2 // Crew
                -> return MainHistoryFragment()
                3 // Reviews
                -> return MainStatusFragment()
                4 // Videos
                -> return MainLogsFragment()
                else -> return null
            }
        }

        // Returns the page title for the top indicator
        override fun getPageTitle(position: Int): CharSequence? {
            return if (mContext != null) {
                when (position) {
                    0 // Details
                    -> mContext.getString(R.string.main_dashboard_tab_sensors)
                    1 // Cast
                    -> mContext.getString(R.string.main_dashboard_tab_actuators)
                    2 // Crew
                    -> mContext.getString(R.string.main_dashboard_tab_history)
                    3 // Reviews
                    -> mContext.getString(R.string.main_dashboard_tab_status)
                    4 // Videos
                    -> mContext.getString(R.string.main_dashboard_tab_logs)
                    else -> null
                }
            } else {
                ""
            }
        }

        companion object {

            private val NUM_ITEMS = 5
        }

    }

}
