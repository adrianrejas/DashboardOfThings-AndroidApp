package com.arejas.dashboardofthings.presentation.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.ActivitySensorListBinding
import com.arejas.dashboardofthings.databinding.ItemSensorListBinding
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorListViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsFragment
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveSensorDialogFragment
import com.arejas.dashboardofthings.utils.Utils
import com.google.android.material.navigation.NavigationView

import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu

import javax.inject.Inject

import dagger.android.AndroidInjection

/**
 * An activity representing a list of Sensors. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [SensorDetailsActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class SensorListActivity : AppCompatActivity(), View.OnClickListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var mTwoPane: Boolean = false

    private var sensorListViewModel: SensorListViewModel? = null

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    private var menu: Menu? = null

    private var navView: NavigationView? = null
    private var drawerLayout: DrawerLayout? = null

    private var currentListShown: LiveData<Resource<List<SensorExtended>>>? = null
    private var glm_grid: GridLayoutManager? = null
    private var mAdapter: SensorListAdapter? = null

    internal var uiBinding: ActivitySensorListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_sensor_list)

        /* Inject dependencies*/
        AndroidInjection.inject(this)

        setSupportActionBar(uiBinding!!.toolbar)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.navigation)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        if (findViewById<View>(R.id.sensor_detail_container) != null) {
            mTwoPane = true
        }

        /* Get view model*/
        sensorListViewModel =
            ViewModelProviders.of(this, this.viewModelFactory).get(SensorListViewModel::class.java)

        drawerLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        navView = findViewById<View>(R.id.navigation_view) as NavigationView
        navView!!.setNavigationItemSelectedListener { menuItem ->
            val fragmentTransaction = false
            val fragment: Fragment? = null

            var intent: Intent? = null
            when (menuItem.itemId) {
                R.id.main_navigation_dashboard -> {
                    intent = Intent(
                        applicationContext,
                        MainDashboardActivity::class.java
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
                R.id.main_navigation_actuators -> {
                    intent = Intent(
                        applicationContext,
                        ActuatorListActivity::class.java
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                R.id.main_navigation_sensors -> {
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

        // Configure adapter for recycler view
        configureListAdapter()

        // Set action of refreshing list when refreshing gesture detected
        uiBinding!!.sensorContainer.srlRefreshLayout.setOnRefreshListener({ setList(false, true) })

        uiBinding!!.listener = this

        // Load sensor list
        setList(true, false)

        // If sensor ID passed at the beginning and in two panel mode, load the sensor in the details area
        if (intent != null && intent.extras != null &&
            intent.extras!!.containsKey(SensorDetailsFragment.SENSOR_ID)
        ) {
            val sensorIdToLoadAtInit = intent.getIntExtra(SensorDetailsFragment.SENSOR_ID, -1)
            if (mTwoPane) {
                val arguments = Bundle()
                arguments.putInt(SensorDetailsFragment.SENSOR_ID, sensorIdToLoadAtInit)
                arguments.putBoolean(SensorDetailsFragment.TWO_PANE, true)
                val fragment = SensorDetailsFragment()
                fragment.arguments = arguments
                supportFragmentManager.beginTransaction()
                    .replace(R.id.sensor_detail_container, fragment)
                    .commit()
            }
        }
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
     * Function called for setting a new list, requesting it to the Database
     *
     * @param showLoading true if wanted to show the loading layout until info got (we don't want in
     * case of swipe refresh, because it has it's own way to info about the loading process).
     * @param refreshData true if wanted to reload the data, false if it's enough with cached data.
     */
    private fun setList(showLoading: Boolean, refreshData: Boolean) {
        if (showLoading) {
            showLoading()
        }
        if (currentListShown != null) {
            currentListShown!!.removeObservers(this)
            currentListShown = null
        }
        currentListShown = sensorListViewModel!!.getListOfSensors(refreshData)
        if (currentListShown != null) {
            currentListShown!!.observe(this, { listResource ->
                if (listResource == null) {
                    showError()
                } else {
                    if (listResource!!.getStatus() == Resource.Status.ERROR) {
                        showError()
                        uiBinding!!.sensorContainer.srlRefreshLayout.setRefreshing(false)
                    } else if (listResource!!.getStatus() == Resource.Status.LOADING) {
                        if (showLoading)
                            showLoading()
                    } else {
                        updateList(listResource!!.data)
                        uiBinding!!.sensorContainer.srlRefreshLayout.setRefreshing(false)
                    }
                }
            })
        }
    }

    private fun updateList(newList: List<SensorExtended>?) {
        if (newList != null && !newList.isEmpty()) {
            showList()
            mAdapter!!.data = newList
            mAdapter!!.notifyDataSetChanged()
        } else {
            showNoElements()
        }
    }

    private fun configureListAdapter() {

        // Configure recycler view with a grid layout
        glm_grid = GridLayoutManager(applicationContext, 1)
        uiBinding!!.sensorContainer.sensorsListListLayout.mainList.setLayoutManager(glm_grid)

        // Configure adapter for recycler view
        mAdapter = SensorListAdapter(this, sensorListViewModel, mTwoPane)
        uiBinding!!.sensorContainer.sensorsListListLayout.mainList.setAdapter(mAdapter)
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private fun showError() {
        if (uiBinding != null) {
            uiBinding!!.sensorContainer.sensorsListListLayout.listLayout.setVisibility(View.GONE)
            uiBinding!!.sensorContainer.sensorsListLoadingLayout.loadingLayout.setVisibility(View.GONE)
            uiBinding!!.sensorContainer.sensorsListErrorLayout.errorLayout.setVisibility(View.VISIBLE)
            uiBinding!!.sensorContainer.sensorsListNoElementsLayout.noElementsLayout.setVisibility(
                View.GONE
            )
            uiBinding!!.sensorContainer.sensorsListErrorLayout.tvError.setText(getString(R.string.error_in_list))
        }
    }

    /**
     * Show the activity info is loading.
     */
    private fun showLoading() {
        if (uiBinding != null) {
            uiBinding!!.sensorContainer.sensorsListListLayout.listLayout.setVisibility(View.GONE)
            uiBinding!!.sensorContainer.sensorsListLoadingLayout.loadingLayout.setVisibility(View.VISIBLE)
            uiBinding!!.sensorContainer.sensorsListErrorLayout.errorLayout.setVisibility(View.GONE)
            uiBinding!!.sensorContainer.sensorsListNoElementsLayout.noElementsLayout.setVisibility(
                View.GONE
            )
        }
    }

    /**
     * Show the activity info has no elements.
     */
    private fun showNoElements() {
        if (uiBinding != null) {
            uiBinding!!.sensorContainer.sensorsListListLayout.listLayout.setVisibility(View.GONE)
            uiBinding!!.sensorContainer.sensorsListLoadingLayout.loadingLayout.setVisibility(View.GONE)
            uiBinding!!.sensorContainer.sensorsListErrorLayout.errorLayout.setVisibility(View.GONE)
            uiBinding!!.sensorContainer.sensorsListNoElementsLayout.noElementsLayout.setVisibility(
                View.VISIBLE
            )
        }
    }

    /**
     * Show the activity info with list.
     */
    private fun showList() {
        if (uiBinding != null) {
            uiBinding!!.sensorContainer.sensorsListListLayout.listLayout.setVisibility(View.VISIBLE)
            uiBinding!!.sensorContainer.sensorsListLoadingLayout.loadingLayout.setVisibility(View.GONE)
            uiBinding!!.sensorContainer.sensorsListErrorLayout.errorLayout.setVisibility(View.GONE)
            uiBinding!!.sensorContainer.sensorsListNoElementsLayout.noElementsLayout.setVisibility(
                View.GONE
            )
        }
    }

    override fun onClick(v: View) {
        val intent = Intent(this, SensorAddEditActivity::class.java)
        startActivity(intent)
    }

    class SensorListAdapter internal constructor(
        private val mParentActivity: SensorListActivity,
        private val mViewModel: SensorListViewModel,
        private val mTwoPane: Boolean
    ) : RecyclerView.Adapter<SensorListAdapter.SensorListViewHolder>() {
        var data: List<SensorExtended>? = null

        private val mOnClickListener = View.OnClickListener { view ->
            val item = view.tag as SensorExtended
            if (mTwoPane) {
                val arguments = Bundle()
                arguments.putInt(SensorDetailsFragment.SENSOR_ID, item.id!!)
                arguments.putBoolean(SensorDetailsFragment.TWO_PANE, true)
                val fragment = SensorDetailsFragment()
                fragment.arguments = arguments
                mParentActivity.supportFragmentManager.beginTransaction()
                    .replace(R.id.sensor_detail_container, fragment)
                    .commit()
            } else {
                val context = view.context
                val intent = Intent(context, SensorDetailsActivity::class.java)
                intent.putExtra(SensorDetailsFragment.SENSOR_ID, item.id)

                context.startActivity(intent)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorListViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<ItemSensorListBinding>(
                inflater, R.layout.item_sensor_list,
                parent, false
            )
            return SensorListViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SensorListViewHolder, position: Int) {
            if (data != null && data!!.size > position) {
                val sensor = data!![position]
                holder.sensor = sensor
                holder.binding.sensor = sensor
                holder.binding.presenter = holder
                holder.itemView.tag = sensor
                holder.itemView.setOnClickListener(mOnClickListener)
            }
        }

        override fun getItemCount(): Int {
            return if (data != null)
                data!!.size
            else
                0
        }

        internal inner class SensorListViewHolder(val binding: ItemSensorListBinding) :
            RecyclerView.ViewHolder(binding.root), SensorElementOptionsListener {

            var sensor: Sensor? = null

            override fun optionsClicked(view: View) {
                //creating a popup menu
                val popup = PopupMenu(DotApplication.context, view)
                //inflating menu from xml resource
                popup.inflate(R.menu.menu_element_management_item)
                //adding click listener
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.menu_edit -> {
                            val intent = Intent(
                                mParentActivity.applicationContext,
                                SensorAddEditActivity::class.java
                            )
                            intent.putExtra(SensorAddEditActivity.SENSOR_ID, sensor!!.id)
                            mParentActivity.startActivity(intent)
                        }
                        R.id.menu_remove -> {
                            val dialog = RemoveSensorDialogFragment(sensor, mViewModel)
                            dialog.show(mParentActivity.supportFragmentManager, "removeSensor")
                        }
                    }
                    false
                }
                //displaying the popup
                popup.show()
            }
        }
    }

    interface SensorElementOptionsListener {

        fun optionsClicked(view: View)

    }
}
