package com.arejas.dashboardofthings.presentation.ui.widget

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.ActivitySelectSensorForWidgetBinding
import com.arejas.dashboardofthings.databinding.ItemSensorListBinding
import com.arejas.dashboardofthings.databinding.ItemSensorSelectWidgetBinding
import com.arejas.dashboardofthings.domain.entities.database.Network
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorListViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkListActivity
import com.arejas.dashboardofthings.presentation.ui.activities.SensorAddEditActivity
import com.arejas.dashboardofthings.presentation.ui.activities.SensorDetailsActivity
import com.arejas.dashboardofthings.presentation.ui.activities.SensorListActivity
import com.arejas.dashboardofthings.presentation.ui.activities.SettingsActivity
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsFragment
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveSensorDialogFragment

import javax.inject.Inject

import dagger.android.AndroidInjection

class SelectSensorForWidgetActivity : AppCompatActivity() {

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    private var sensorListViewModel: SensorListViewModel? = null

    internal var uiBinding: ActivitySelectSensorForWidgetBinding? = null
    private var mAdapter: SensorListAdapter? = null
    private var glm_grid: GridLayoutManager? = null
    private var currentListShown: LiveData<Resource<List<SensorExtended>>>? = null
    private var widgetIdToConfigure: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_sensor_for_widget)

        /* Inflate main layout and get UI element references */
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_sensor_for_widget)

        /* Inject dependencies*/
        AndroidInjection.inject(this)

        // If sensor ID passed at the beginning and in two panel mode, load the sensor in the details area
        if (savedInstanceState != null && savedInstanceState.containsKey(WIDGET_ID)) {
            widgetIdToConfigure = savedInstanceState.getInt(WIDGET_ID, -1)
            if (widgetIdToConfigure < 0) widgetIdToConfigure = null
        } else if (intent != null && intent.extras != null &&
            intent.extras!!.containsKey(WIDGET_ID)
        ) {
            widgetIdToConfigure = intent.getIntExtra(WIDGET_ID, -1)
            if (widgetIdToConfigure < 0) widgetIdToConfigure = null
        }
        if (widgetIdToConfigure == null) finish()

        setSupportActionBar(uiBinding!!.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(false)

        /* Get view model*/
        sensorListViewModel =
            ViewModelProviders.of(this, this.viewModelFactory).get(SensorListViewModel::class.java)

        // Configure adapter for recycler view
        configureListAdapter()

        // Load sensor list
        setList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(WIDGET_ID, widgetIdToConfigure!!)
        super.onSaveInstanceState(outState)
    }

    /**
     * Function called for setting a new sensor list
     *
     */
    private fun setList() {
        showLoading()
        if (currentListShown != null) {
            currentListShown!!.removeObservers(this)
            currentListShown = null
        }
        currentListShown = sensorListViewModel!!.getListOfSensors(true)
        if (currentListShown != null) {
            currentListShown!!.observe(this, { listResource ->
                if (listResource == null) {
                    showError()
                } else {
                    if (listResource!!.getStatus() == Resource.Status.ERROR) {
                        showError()
                    } else if (listResource!!.getStatus() == Resource.Status.LOADING) {
                        showLoading()
                    } else {
                        updateList(listResource!!.data)
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
        uiBinding!!.selectSensorWidgetListLayout.mainList.layoutManager = glm_grid

        // Configure adapter for recycler view
        mAdapter = SensorListAdapter(this, widgetIdToConfigure)
        uiBinding!!.selectSensorWidgetListLayout.mainList.adapter = mAdapter
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private fun showError() {
        if (uiBinding != null) {
            uiBinding!!.selectSensorWidgetListLayout.listLayout.visibility = View.GONE
            uiBinding!!.selectSensorWidgetLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.selectSensorWidgetErrorLayout.errorLayout.visibility = View.VISIBLE
            uiBinding!!.selectSensorWidgetNoElementsLayout.noElementsLayout.visibility = View.GONE
            uiBinding!!.selectSensorWidgetErrorLayout.tvError.text =
                getString(R.string.error_in_list)
        }
    }

    /**
     * Show the activity info is loading.
     */
    private fun showLoading() {
        if (uiBinding != null) {
            uiBinding!!.selectSensorWidgetListLayout.listLayout.visibility = View.GONE
            uiBinding!!.selectSensorWidgetLoadingLayout.loadingLayout.visibility = View.VISIBLE
            uiBinding!!.selectSensorWidgetErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.selectSensorWidgetNoElementsLayout.noElementsLayout.visibility = View.GONE
        }
    }

    /**
     * Show the activity info has no elements.
     */
    private fun showNoElements() {
        if (uiBinding != null) {
            uiBinding!!.selectSensorWidgetListLayout.listLayout.visibility = View.GONE
            uiBinding!!.selectSensorWidgetLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.selectSensorWidgetErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.selectSensorWidgetNoElementsLayout.noElementsLayout.visibility =
                View.VISIBLE
        }
    }

    /**
     * Show the activity info with list.
     */
    private fun showList() {
        if (uiBinding != null) {
            uiBinding!!.selectSensorWidgetListLayout.listLayout.visibility = View.VISIBLE
            uiBinding!!.selectSensorWidgetLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.selectSensorWidgetErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.selectSensorWidgetNoElementsLayout.noElementsLayout.visibility = View.GONE
        }
    }

    class SensorListAdapter internal constructor(
        private val mParentActivity: Activity,
        private val widgetId: Int?
    ) : RecyclerView.Adapter<SensorListAdapter.SensorListViewHolder>() {
        var data: List<SensorExtended>? = null

        private val mOnClickListener = View.OnClickListener { view ->
            val item = view.tag as SensorExtended
            val preferences = PreferenceManager.getDefaultSharedPreferences(mParentActivity)
            SensorWidgetService.startActionSetSensorForWidget(
                mParentActivity.applicationContext,
                widgetId!!,
                item.id!!
            )
            mParentActivity.finish()
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): SensorListAdapter.SensorListViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<ItemSensorSelectWidgetBinding>(
                inflater, R.layout.item_sensor_select_widget,
                parent, false
            )
            return SensorListViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SensorListViewHolder, position: Int) {
            if (data != null && data!!.size > position) {
                val sensor = data!![position]
                holder.sensor = sensor
                holder.binding.sensor = sensor
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

        internal inner class SensorListViewHolder(val binding: ItemSensorSelectWidgetBinding) :
            RecyclerView.ViewHolder(binding.root) {

            var sensor: Sensor? = null

        }
    }

    companion object {

        val WIDGET_ID = "widget_id"
    }

}
