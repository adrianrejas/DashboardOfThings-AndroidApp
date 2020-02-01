package com.arejas.dashboardofthings.presentation.ui.activities

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.lifecycle.Observer

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.ActivityActuatorListBinding
import com.arejas.dashboardofthings.databinding.ItemActuatorListBinding
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorListViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.fragments.ActuatorDetailsFragment
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveActuatorDialogFragment
import com.arejas.dashboardofthings.utils.Utils
import com.google.android.material.navigation.NavigationView

import javax.inject.Inject

import dagger.android.AndroidInjection

class ActuatorListActivity : AppCompatActivity(), View.OnClickListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private var mTwoPane: Boolean = false

    private var actuatorListViewModel: ActuatorListViewModel? = null

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    private var menu: Menu? = null

    private var navView: NavigationView? = null
    private var drawerLayout: DrawerLayout? = null

    private var currentListShown: LiveData<Resource<List<ActuatorExtended>>>? = null
    private var glm_grid: GridLayoutManager? = null
    private var mAdapter: ActuatorListActivity.ActuatorListAdapter? = null

    internal var uiBinding: ActivityActuatorListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_actuator_list)

        /* Inject dependencies*/
        AndroidInjection.inject(this)

        setSupportActionBar(uiBinding!!.toolbar)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.navigation)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        if (findViewById<View>(R.id.actuator_detail_container) != null) {
            mTwoPane = true
        }

        /* Get view model*/
        actuatorListViewModel = ViewModelProviders.of(this, this.viewModelFactory)
            .get(ActuatorListViewModel::class.java)

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
        uiBinding!!.actuatorContainer.srlRefreshLayout.setOnRefreshListener({
            setList(
                false,
                true
            )
        })

        uiBinding!!.listener = this

        // Load actuator list
        setList(true, false)

        // If actuator ID passed at the beginning and in two panel mode, load the actuator in the details area
        if (intent != null && intent.extras != null &&
            intent.extras!!.containsKey(ActuatorDetailsFragment.ACTUATOR_ID)
        ) {
            val actuatorIdToLoadAtInit = intent.getIntExtra(ActuatorDetailsFragment.ACTUATOR_ID, -1)
            if (mTwoPane) {
                val arguments = Bundle()
                arguments.putInt(ActuatorDetailsFragment.ACTUATOR_ID, actuatorIdToLoadAtInit)
                arguments.putBoolean(ActuatorDetailsFragment.TWO_PANE, true)
                val fragment = ActuatorDetailsFragment()
                fragment.arguments = arguments
                supportFragmentManager.beginTransaction()
                    .replace(R.id.actuator_detail_container, fragment)
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
        currentListShown = actuatorListViewModel!!.getListOfActuators(refreshData)
        if (currentListShown != null) {
            currentListShown!!.observe(this, Observer { listResource ->
                if (listResource == null) {
                    showError()
                } else {
                    if (listResource!!.status == Resource.Status.ERROR) {
                        showError()
                        uiBinding!!.actuatorContainer.srlRefreshLayout.setRefreshing(false)
                    } else if (listResource!!.status == Resource.Status.LOADING) {
                        if (showLoading)
                            showLoading()
                    } else {
                        updateList(listResource!!.data)
                        uiBinding!!.actuatorContainer.srlRefreshLayout.setRefreshing(false)
                    }
                }
            })
        }
    }

    private fun updateList(newList: List<ActuatorExtended>?) {
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
        uiBinding!!.actuatorContainer.actuatorsListListLayout.mainList.setLayoutManager(glm_grid)

        // Configure adapter for recycler view
        mAdapter = ActuatorListActivity.ActuatorListAdapter(this, actuatorListViewModel, mTwoPane)
        uiBinding!!.actuatorContainer.actuatorsListListLayout.mainList.setAdapter(mAdapter)
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private fun showError() {
        if (uiBinding != null) {
            uiBinding!!.actuatorContainer.actuatorsListListLayout.listLayout.setVisibility(View.GONE)
            uiBinding!!.actuatorContainer.actuatorsListLoadingLayout.loadingLayout.setVisibility(
                View.GONE
            )
            uiBinding!!.actuatorContainer.actuatorsListErrorLayout.errorLayout.setVisibility(View.VISIBLE)
            uiBinding!!.actuatorContainer.actuatorsListNoElementsLayout.noElementsLayout.setVisibility(
                View.GONE
            )
            uiBinding!!.actuatorContainer.actuatorsListErrorLayout.tvError.setText(getString(R.string.error_in_list))
        }
    }

    /**
     * Show the activity info is loading.
     */
    private fun showLoading() {
        if (uiBinding != null) {
            uiBinding!!.actuatorContainer.actuatorsListListLayout.listLayout.setVisibility(View.GONE)
            uiBinding!!.actuatorContainer.actuatorsListLoadingLayout.loadingLayout.setVisibility(
                View.VISIBLE
            )
            uiBinding!!.actuatorContainer.actuatorsListErrorLayout.errorLayout.setVisibility(View.GONE)
            uiBinding!!.actuatorContainer.actuatorsListNoElementsLayout.noElementsLayout.setVisibility(
                View.GONE
            )
        }
    }

    /**
     * Show the activity info has no elements.
     */
    private fun showNoElements() {
        if (uiBinding != null) {
            uiBinding!!.actuatorContainer.actuatorsListListLayout.listLayout.setVisibility(View.GONE)
            uiBinding!!.actuatorContainer.actuatorsListLoadingLayout.loadingLayout.setVisibility(
                View.GONE
            )
            uiBinding!!.actuatorContainer.actuatorsListErrorLayout.errorLayout.setVisibility(View.GONE)
            uiBinding!!.actuatorContainer.actuatorsListNoElementsLayout.noElementsLayout.setVisibility(
                View.VISIBLE
            )
        }
    }

    /**
     * Show the activity info with list.
     */
    private fun showList() {
        if (uiBinding != null) {
            uiBinding!!.actuatorContainer.actuatorsListListLayout.listLayout.setVisibility(View.VISIBLE)
            uiBinding!!.actuatorContainer.actuatorsListLoadingLayout.loadingLayout.setVisibility(
                View.GONE
            )
            uiBinding!!.actuatorContainer.actuatorsListErrorLayout.errorLayout.setVisibility(View.GONE)
            uiBinding!!.actuatorContainer.actuatorsListNoElementsLayout.noElementsLayout.setVisibility(
                View.GONE
            )
        }
    }

    override fun onClick(v: View) {
        val intent = Intent(this, ActuatorAddEditActivity::class.java)
        startActivity(intent)
    }

    internal class ActuatorListAdapter internal constructor(
        private val mParentActivity: ActuatorListActivity,
        private val mViewModel: ActuatorListViewModel,
        private val mTwoPane: Boolean
    ) : RecyclerView.Adapter<ActuatorListActivity.ActuatorListAdapter.ActuatorListViewHolder>() {
        var data: List<ActuatorExtended>? = null

        private val mOnClickListener = View.OnClickListener { view ->
            val item = view.tag as ActuatorExtended
            if (mTwoPane) {
                val arguments = Bundle()
                arguments.putInt(ActuatorDetailsFragment.ACTUATOR_ID, item.id!!)
                arguments.putBoolean(ActuatorDetailsFragment.TWO_PANE, true)
                val fragment = ActuatorDetailsFragment()
                fragment.arguments = arguments
                mParentActivity.supportFragmentManager.beginTransaction()
                    .replace(R.id.actuator_detail_container, fragment)
                    .commit()
            } else {
                val context = view.context
                val intent = Intent(context, ActuatorDetailsActivity::class.java)
                intent.putExtra(ActuatorDetailsFragment.ACTUATOR_ID, item.id)
                context.startActivity(intent)
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ActuatorListActivity.ActuatorListAdapter.ActuatorListViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<ItemActuatorListBinding>(
                inflater, R.layout.item_actuator_list,
                parent, false
            )
            return ActuatorListActivity.ActuatorListAdapter.ActuatorListViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: ActuatorListActivity.ActuatorListAdapter.ActuatorListViewHolder,
            position: Int
        ) {
            if (data != null && data!!.size > position) {
                val actuator = data!![position]
                holder.actuator = actuator
                holder.binding.actuator = actuator
                holder.binding.presenter = holder
                holder.itemView.tag = actuator
                holder.itemView.setOnClickListener(mOnClickListener)
            }
        }

        override fun getItemCount(): Int {
            return if (data != null)
                data!!.size
            else
                0
        }

        internal inner class ActuatorListViewHolder(val binding: ItemActuatorListBinding) :
            RecyclerView.ViewHolder(binding.root),
            ActuatorListActivity.ActuatorElementOptionsListener {

            var actuator: Actuator? = null

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
                                ActuatorAddEditActivity::class.java
                            )
                            intent.putExtra(ActuatorAddEditActivity.ACTUATOR_ID, actuator!!.id)
                            mParentActivity.startActivity(intent)
                        }
                        R.id.menu_remove -> {
                            val dialog = RemoveActuatorDialogFragment(actuator, mViewModel)
                            dialog.show(mParentActivity.supportFragmentManager, "removeActuator")
                        }
                    }
                    false
                }
                //displaying the popup
                popup.show()
            }
        }
    }

    interface ActuatorElementOptionsListener {

        fun optionsClicked(view: View)

    }

}
