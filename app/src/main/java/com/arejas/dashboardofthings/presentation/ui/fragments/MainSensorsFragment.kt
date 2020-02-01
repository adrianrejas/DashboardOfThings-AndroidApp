package com.arejas.dashboardofthings.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.CardMaindashboardSensorBinding
import com.arejas.dashboardofthings.databinding.FragmentMainsensorsBinding
import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MainDashboardViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper

import java.util.HashMap
import java.util.Objects

import javax.inject.Inject

import dagger.android.support.AndroidSupportInjection

class MainSensorsFragment : Fragment() {

    internal var uiBinding: FragmentMainsensorsBinding? = null

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    private var mainnetwork_addeditViewModel: MainDashboardViewModel? = null

    private var currentListShown: LiveData<Resource<List<SensorExtended>>>? = null
    private var dataValuesReceivedManaged: LiveData<Resource<List<DataValue>>>? = null
    private var glm_grid: StaggeredGridLayoutManager? = null
    private var mAdapter: SensorsListAdapter? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Inject dependencies
        AndroidSupportInjection.inject(this)
        // Get the main dashboard activity view model and observe the changes in the details
        mainnetwork_addeditViewModel = ViewModelProviders.of(
            Objects.requireNonNull<FragmentActivity>(activity),
            viewModelFactory
        ).get(MainDashboardViewModel::class.java)
        setList(true, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uiBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_mainsensors, container, false)
        // Configure adapter for recycler view
        configureListAdapter()
        // Set action of refreshing list when refreshing gesture detected
        uiBinding!!.srlRefreshLayout.setOnRefreshListener { setList(false, true) }
        return uiBinding!!.root
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
        currentListShown = mainnetwork_addeditViewModel!!.getListOfSensorsMainDashboard(refreshData)
        if (currentListShown != null) {
            currentListShown!!.observe(this, { listResource ->
                if (listResource == null) {
                    showError()
                } else {
                    if (listResource!!.getStatus() == Resource.Status.ERROR) {
                        showError()
                        uiBinding!!.srlRefreshLayout.isRefreshing = false
                    } else if (listResource!!.getStatus() == Resource.Status.LOADING) {
                        if (showLoading)
                            showLoading()
                    } else {
                        updateList(listResource!!.data)
                        uiBinding!!.srlRefreshLayout.isRefreshing = false
                    }
                }
            })
        }
        if (dataValuesReceivedManaged != null) {
            dataValuesReceivedManaged!!.removeObservers(this)
            dataValuesReceivedManaged = null
        }
        dataValuesReceivedManaged =
            mainnetwork_addeditViewModel!!.getListOfSensorsInDashboardLastValues(refreshData)
        if (dataValuesReceivedManaged != null) {
            dataValuesReceivedManaged!!.observe(this, { listResource ->
                if (listResource == null) {
                    showError()
                } else {
                    if (listResource!!.getStatus() == Resource.Status.SUCCESS && listResource!!.data != null) {
                        updateDataValues(listResource!!.data)
                    }
                }
            })
        }
    }

    private fun updateList(newList: List<SensorExtended>?) {
        if (newList != null && !newList.isEmpty()) {
            showList()
            mAdapter!!.sensors = newList
            mAdapter!!.notifyDataSetChanged()
        } else {
            showNoElements()
        }
    }

    private fun updateDataValues(newDataValueList: List<DataValue>?) {
        var newDataValueMap: MutableMap<Int, DataValue>? = mAdapter!!.dataValues
        if (newDataValueMap != null) {
            newDataValueMap.clear()
        } else {
            newDataValueMap = HashMap()
        }
        for (value in newDataValueList!!) {
            newDataValueMap[value.sensorId!!] = value
        }
        mAdapter!!.dataValues = newDataValueMap
        mAdapter!!.notifyDataSetChanged()
    }

    private fun configureListAdapter() {

        /* Get number of items in a row*/
        val iElementsPerRow = resources.getInteger(R.integer.list_maindash_column_count)

        // Configure recycler view with a grid layout
        glm_grid = StaggeredGridLayoutManager(iElementsPerRow, StaggeredGridLayoutManager.VERTICAL)
        uiBinding!!.sensorsMainListListLayout.mainList.layoutManager = glm_grid

        // Configure adapter for recycler view
        mAdapter = SensorsListAdapter()
        uiBinding!!.sensorsMainListListLayout.mainList.adapter = mAdapter
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private fun showError() {
        if (uiBinding != null) {
            uiBinding!!.sensorsMainListListLayout.listLayout.visibility = View.GONE
            uiBinding!!.sensorsMainListLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.sensorsMainListErrorLayout.errorLayout.visibility = View.VISIBLE
            uiBinding!!.sensorsMainListNoElementsLayout.noElementsLayout.visibility = View.GONE
            uiBinding!!.sensorsMainListErrorLayout.tvError.text = getString(R.string.error_in_list)
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private fun showLoading() {
        if (uiBinding != null) {
            uiBinding!!.sensorsMainListListLayout.listLayout.visibility = View.GONE
            uiBinding!!.sensorsMainListLoadingLayout.loadingLayout.visibility = View.VISIBLE
            uiBinding!!.sensorsMainListErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.sensorsMainListNoElementsLayout.noElementsLayout.visibility = View.GONE
        }
    }

    /**
     * Show the fragment info has no elements.
     */
    private fun showNoElements() {
        if (uiBinding != null) {
            uiBinding!!.sensorsMainListListLayout.listLayout.visibility = View.GONE
            uiBinding!!.sensorsMainListLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.sensorsMainListErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.sensorsMainListNoElementsLayout.noElementsLayout.visibility = View.VISIBLE
        }
    }

    /**
     * Show the fragment info with list.
     */
    private fun showList() {
        if (uiBinding != null) {
            uiBinding!!.sensorsMainListListLayout.listLayout.visibility = View.VISIBLE
            uiBinding!!.sensorsMainListLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.sensorsMainListErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.sensorsMainListNoElementsLayout.noElementsLayout.visibility = View.GONE
        }
    }

    internal inner class SensorsListAdapter :
        RecyclerView.Adapter<SensorsListAdapter.SensorListViewHolder>() {

        var sensors: List<SensorExtended>? = null

        private var mDataValues: Map<Int, DataValue>? = null

        var dataValues: MutableMap<Int, DataValue>?
            get() = mDataValues
            set(mDataValues) {
                this.mDataValues = mDataValues
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorListViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<CardMaindashboardSensorBinding>(
                inflater, R.layout.card_maindashboard_sensor,
                parent, false
            )
            return SensorListViewHolder(binding)
        }

        override fun onBindViewHolder(holder: SensorListViewHolder, position: Int) {
            if (sensors != null && sensors!!.size > position) {
                holder.itemView.tag = position
                val sensor = sensors!![position]
                if (sensor != null) {
                    holder.sensor = sensor
                    holder.binding.sensor = sensor
                    holder.binding.presenter = holder
                    if (mDataValues != null && mDataValues!!.containsKey(sensor.id)) {
                        holder.binding.lastValue = mDataValues!![sensor.id]!!.value
                    }
                }
            }
        }

        override fun getItemCount(): Int {
            return if (sensors != null) {
                sensors!!.size
            } else {
                0
            }
        }

        internal inner class SensorListViewHolder(val binding: CardMaindashboardSensorBinding) :
            RecyclerView.ViewHolder(binding.root), SensorMainDashboardListener {

            var sensor: Sensor? = null

            override fun requestReload() {
                if (sensor != null) {
                    mainnetwork_addeditViewModel!!.requestSensorReload(sensor)
                }
            }
        }

    }

    interface SensorMainDashboardListener {

        fun requestReload()

    }

}
