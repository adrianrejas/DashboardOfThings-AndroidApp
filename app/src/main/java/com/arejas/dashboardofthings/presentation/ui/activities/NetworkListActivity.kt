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
import com.arejas.dashboardofthings.databinding.ActivityNetworkListBinding
import com.arejas.dashboardofthings.databinding.ItemNetworkListBinding
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkListViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailsFragment
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveNetworkDialogFragment
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
 * An activity representing a list of Networks. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a [NetworkDetailsActivity] representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
class NetworkListActivity : AppCompatActivity(), View.OnClickListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var mTwoPane: Boolean = false

    private var networkListViewModel: NetworkListViewModel? = null

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    private var menu: Menu? = null

    private var navView: NavigationView? = null
    private var drawerLayout: DrawerLayout? = null

    private var currentListShown: LiveData<Resource<List<NetworkExtended>>>? = null
    private var glm_grid: GridLayoutManager? = null
    private var mAdapter: NetworkListAdapter? = null

    internal var uiBinding: ActivityNetworkListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_network_list)

        /* Inject dependencies*/
        AndroidInjection.inject(this)

        setSupportActionBar(uiBinding!!.toolbar)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.navigation)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        if (findViewById<View>(R.id.network_detail_container) != null) {
            mTwoPane = true
        }

        /* Get view model*/
        networkListViewModel =
            ViewModelProviders.of(this, this.viewModelFactory).get(NetworkListViewModel::class.java)

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
        uiBinding!!.networkContainer.srlRefreshLayout.setOnRefreshListener({ setList(false, true) })

        uiBinding!!.listener = this

        // Load network list
        setList(true, false)
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
        currentListShown = networkListViewModel!!.getListOfNetworks(refreshData)
        if (currentListShown != null) {
            currentListShown!!.observe(this, { listResource ->
                if (listResource == null) {
                    showError()
                } else {
                    if (listResource!!.getStatus() == Resource.Status.ERROR) {
                        showError()
                        uiBinding!!.networkContainer.srlRefreshLayout.setRefreshing(false)
                    } else if (listResource!!.getStatus() == Resource.Status.LOADING) {
                        if (showLoading)
                            showLoading()
                    } else {
                        updateList(listResource!!.data)
                        uiBinding!!.networkContainer.srlRefreshLayout.setRefreshing(false)
                    }
                }
            })
        }
    }

    private fun updateList(newList: List<NetworkExtended>?) {
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
        uiBinding!!.networkContainer.networksListListLayout.mainList.setLayoutManager(glm_grid)

        // Configure adapter for recycler view
        mAdapter = NetworkListAdapter(this, networkListViewModel, mTwoPane)
        uiBinding!!.networkContainer.networksListListLayout.mainList.setAdapter(mAdapter)
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private fun showError() {
        if (uiBinding != null) {
            uiBinding!!.networkContainer.networksListListLayout.listLayout.setVisibility(View.GONE)
            uiBinding!!.networkContainer.networksListLoadingLayout.loadingLayout.setVisibility(View.GONE)
            uiBinding!!.networkContainer.networksListErrorLayout.errorLayout.setVisibility(View.VISIBLE)
            uiBinding!!.networkContainer.networksListNoElementsLayout.noElementsLayout.setVisibility(
                View.GONE
            )
            uiBinding!!.networkContainer.networksListErrorLayout.tvError.setText(getString(R.string.error_in_list))
        }
    }

    /**
     * Show the activity info is loading.
     */
    private fun showLoading() {
        if (uiBinding != null) {
            uiBinding!!.networkContainer.networksListListLayout.listLayout.setVisibility(View.GONE)
            uiBinding!!.networkContainer.networksListLoadingLayout.loadingLayout.setVisibility(View.VISIBLE)
            uiBinding!!.networkContainer.networksListErrorLayout.errorLayout.setVisibility(View.GONE)
            uiBinding!!.networkContainer.networksListNoElementsLayout.noElementsLayout.setVisibility(
                View.GONE
            )
        }
    }

    /**
     * Show the activity info has no elements.
     */
    private fun showNoElements() {
        if (uiBinding != null) {
            uiBinding!!.networkContainer.networksListListLayout.listLayout.setVisibility(View.GONE)
            uiBinding!!.networkContainer.networksListLoadingLayout.loadingLayout.setVisibility(View.GONE)
            uiBinding!!.networkContainer.networksListErrorLayout.errorLayout.setVisibility(View.GONE)
            uiBinding!!.networkContainer.networksListNoElementsLayout.noElementsLayout.setVisibility(
                View.VISIBLE
            )
        }
    }

    /**
     * Show the activity info with list.
     */
    private fun showList() {
        if (uiBinding != null) {
            uiBinding!!.networkContainer.networksListListLayout.listLayout.setVisibility(View.VISIBLE)
            uiBinding!!.networkContainer.networksListLoadingLayout.loadingLayout.setVisibility(View.GONE)
            uiBinding!!.networkContainer.networksListErrorLayout.errorLayout.setVisibility(View.GONE)
            uiBinding!!.networkContainer.networksListNoElementsLayout.noElementsLayout.setVisibility(
                View.GONE
            )
        }
    }

    override fun onClick(v: View) {
        val intent = Intent(this, NetworkAddEditActivity::class.java)
        startActivity(intent)
    }

    class NetworkListAdapter internal constructor(
        private val mParentActivity: NetworkListActivity,
        private val mViewModel: NetworkListViewModel,
        private val mTwoPane: Boolean
    ) : RecyclerView.Adapter<NetworkListAdapter.NetworkListViewHolder>() {
        var data: List<NetworkExtended>? = null

        private val mOnClickListener = View.OnClickListener { view ->
            val item = view.tag as NetworkExtended
            if (mTwoPane) {
                val arguments = Bundle()
                arguments.putInt(NetworkDetailsFragment.NETWORK_ID, item.id!!)
                arguments.putBoolean(NetworkDetailsFragment.TWO_PANE, true)
                val fragment = NetworkDetailsFragment()
                fragment.arguments = arguments
                mParentActivity.supportFragmentManager.beginTransaction()
                    .replace(R.id.network_detail_container, fragment)
                    .commit()
            } else {
                val context = view.context
                val intent = Intent(context, NetworkDetailsActivity::class.java)
                intent.putExtra(NetworkDetailsFragment.NETWORK_ID, item.id)

                context.startActivity(intent)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NetworkListViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<ItemNetworkListBinding>(
                inflater, R.layout.item_network_list,
                parent, false
            )
            return NetworkListViewHolder(binding)
        }

        override fun onBindViewHolder(holder: NetworkListViewHolder, position: Int) {
            if (data != null && data!!.size > position) {
                val network = data!![position]
                holder.network = network
                holder.binding.network = network
                holder.binding.presenter = holder
                holder.itemView.tag = network
                holder.itemView.setOnClickListener(mOnClickListener)
            }
        }

        override fun getItemCount(): Int {
            return if (data != null)
                data!!.size
            else
                0
        }

        internal inner class NetworkListViewHolder(val binding: ItemNetworkListBinding) :
            RecyclerView.ViewHolder(binding.root), NetworkElementOptionsListener {

            var network: Network? = null

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
                                NetworkAddEditActivity::class.java
                            )
                            intent.putExtra(NetworkAddEditActivity.NETWORK_ID, network!!.id)
                            mParentActivity.startActivity(intent)
                        }
                        R.id.menu_remove -> {
                            val dialog = RemoveNetworkDialogFragment(network, mViewModel)
                            dialog.show(mParentActivity.supportFragmentManager, "removeNetwork")
                        }
                    }
                    false
                }
                //displaying the popup
                popup.show()
            }
        }
    }

    interface NetworkElementOptionsListener {

        fun optionsClicked(view: View)

    }
}
