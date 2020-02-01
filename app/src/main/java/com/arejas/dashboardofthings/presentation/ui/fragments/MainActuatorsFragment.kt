package com.arejas.dashboardofthings.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.CardMaindashboardActuatorBinding
import com.arejas.dashboardofthings.databinding.FragmentMainactuatorsBinding
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.MainDashboardViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper

import java.util.HashMap
import java.util.Objects

import javax.inject.Inject

import dagger.android.support.AndroidSupportInjection

class MainActuatorsFragment : Fragment() {

    internal var uiBinding: FragmentMainactuatorsBinding? = null

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    private var mainnetwork_addeditViewModel: MainDashboardViewModel? = null

    private var currentListShown: LiveData<Resource<List<ActuatorExtended>>>? = null
    private var dataValuesReceivedManaged: LiveData<Resource<List<DataValue>>>? = null
    private var glm_grid: StaggeredGridLayoutManager? = null
    private var mAdapter: MainActuatorsFragment.ActuatorsListAdapter? = null

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
            DataBindingUtil.inflate(inflater, R.layout.fragment_mainactuators, container, false)
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
        currentListShown =
            mainnetwork_addeditViewModel!!.getListOfActuatorsMainDashboard(refreshData)
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
    }

    private fun updateList(newList: List<ActuatorExtended>?) {
        if (newList != null && !newList.isEmpty()) {
            showList()
            mAdapter!!.actuators = newList
            mAdapter!!.notifyDataSetChanged()
        } else {
            showNoElements()
        }
    }

    private fun configureListAdapter() {

        /* Get number of items in a row*/
        val iElementsPerRow = resources.getInteger(R.integer.list_maindash_column_count)

        // Configure recycler view with a grid layout
        glm_grid = StaggeredGridLayoutManager(iElementsPerRow, StaggeredGridLayoutManager.VERTICAL)
        uiBinding!!.actuatorsMainListListLayout.mainList.layoutManager = glm_grid

        // Configure adapter for recycler view
        mAdapter = MainActuatorsFragment.ActuatorsListAdapter()
        uiBinding!!.actuatorsMainListListLayout.mainList.adapter = mAdapter
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private fun showError() {
        if (uiBinding != null) {
            uiBinding!!.actuatorsMainListListLayout.listLayout.visibility = View.GONE
            uiBinding!!.actuatorsMainListLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.actuatorsMainListErrorLayout.errorLayout.visibility = View.VISIBLE
            uiBinding!!.actuatorsMainListNoElementsLayout.noElementsLayout.visibility = View.GONE
            uiBinding!!.actuatorsMainListErrorLayout.tvError.text =
                getString(R.string.error_in_list)
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private fun showLoading() {
        if (uiBinding != null) {
            uiBinding!!.actuatorsMainListListLayout.listLayout.visibility = View.GONE
            uiBinding!!.actuatorsMainListLoadingLayout.loadingLayout.visibility = View.VISIBLE
            uiBinding!!.actuatorsMainListErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.actuatorsMainListNoElementsLayout.noElementsLayout.visibility = View.GONE
        }
    }

    /**
     * Show the fragment info has no elements.
     */
    private fun showNoElements() {
        if (uiBinding != null) {
            uiBinding!!.actuatorsMainListListLayout.listLayout.visibility = View.GONE
            uiBinding!!.actuatorsMainListLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.actuatorsMainListErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.actuatorsMainListNoElementsLayout.noElementsLayout.visibility = View.VISIBLE
        }
    }

    /**
     * Show the fragment info with list.
     */
    private fun showList() {
        if (uiBinding != null) {
            uiBinding!!.actuatorsMainListListLayout.listLayout.visibility = View.VISIBLE
            uiBinding!!.actuatorsMainListLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.actuatorsMainListErrorLayout.errorLayout.visibility = View.GONE
            uiBinding!!.actuatorsMainListNoElementsLayout.noElementsLayout.visibility = View.GONE
        }
    }

    internal inner class ActuatorsListAdapter :
        RecyclerView.Adapter<MainActuatorsFragment.ActuatorsListAdapter.ActuatorListViewHolder>() {

        var actuators: List<ActuatorExtended>? = null

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MainActuatorsFragment.ActuatorsListAdapter.ActuatorListViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = DataBindingUtil.inflate<CardMaindashboardActuatorBinding>(
                inflater, R.layout.card_maindashboard_actuator,
                parent, false
            )
            return MainActuatorsFragment.ActuatorsListAdapter.ActuatorListViewHolder(binding)
        }

        override fun onBindViewHolder(
            holder: MainActuatorsFragment.ActuatorsListAdapter.ActuatorListViewHolder,
            position: Int
        ) {
            if (actuators != null && actuators!!.size > position) {
                holder.itemView.tag = position
                val actuator = actuators!![position]
                if (actuator != null) {
                    holder.actuator = actuator
                    holder.binding.presenter = holder
                    holder.binding.actuator = actuator
                }
            }
        }

        override fun getItemCount(): Int {
            return if (actuators != null) {
                actuators!!.size
            } else {
                0
            }
        }

        internal inner class ActuatorListViewHolder(val binding: CardMaindashboardActuatorBinding) :
            RecyclerView.ViewHolder(binding.root), ActuatorMainDashboardListener {

            var actuator: Actuator? = null

            override fun sendInteger() {
                try {
                    if (actuator != null) {
                        val data = binding.etActuatorIntegerValue.text.toString()
                        val comparingData = java.lang.Float.valueOf(data)
                        if (actuator!!.dataNumberMinimum != null && actuator!!.dataNumberMaximum != null) {
                            if (comparingData < actuator!!.dataNumberMinimum || comparingData > actuator!!.dataNumberMaximum) {
                                ToastHelper.showToast(
                                    getString(
                                        R.string.set_toast_between,
                                        actuator!!.dataNumberMinimum!!.toString(),
                                        actuator!!.dataNumberMaximum!!.toString()
                                    )
                                )
                                return
                            }
                        } else if (actuator!!.dataNumberMinimum != null) {
                            if (comparingData < actuator!!.dataNumberMinimum) {
                                ToastHelper.showToast(
                                    getString(
                                        R.string.set_toast_smaller_than,
                                        actuator!!.dataNumberMinimum!!.toString()
                                    )
                                )
                                return
                            }
                        } else if (actuator!!.dataNumberMaximum != null) {
                            if (comparingData > actuator!!.dataNumberMaximum) {
                                ToastHelper.showToast(
                                    getString(
                                        R.string.set_toast_bigger_than,
                                        actuator!!.dataNumberMaximum!!.toString()
                                    )
                                )
                                return
                            }
                        }
                        if (data != null)
                            mainnetwork_addeditViewModel!!.sendActuatorData(actuator, data)
                    }
                } catch (e: Exception) {
                    ToastHelper.showToast(getString(R.string.set_toast_error))
                }

            }

            override fun sendFloat() {
                try {
                    if (actuator != null) {
                        val data = binding.etActuatorDecimalValue.text.toString()
                        val comparingData = java.lang.Float.valueOf(data)
                        if (actuator!!.dataNumberMinimum != null && actuator!!.dataNumberMaximum != null) {
                            if (comparingData < actuator!!.dataNumberMinimum || comparingData > actuator!!.dataNumberMaximum) {
                                ToastHelper.showToast(
                                    getString(
                                        R.string.set_toast_between,
                                        actuator!!.dataNumberMinimum!!.toString(),
                                        actuator!!.dataNumberMaximum!!.toString()
                                    )
                                )
                                return
                            }
                        } else if (actuator!!.dataNumberMinimum != null) {
                            if (comparingData < actuator!!.dataNumberMinimum) {
                                ToastHelper.showToast(
                                    getString(
                                        R.string.set_toast_smaller_than,
                                        actuator!!.dataNumberMinimum!!.toString()
                                    )
                                )
                                return
                            }
                        } else if (actuator!!.dataNumberMaximum != null) {
                            if (comparingData > actuator!!.dataNumberMaximum) {
                                ToastHelper.showToast(
                                    getString(
                                        R.string.set_toast_bigger_than,
                                        actuator!!.dataNumberMaximum!!.toString()
                                    )
                                )
                                return
                            }
                        }
                        if (data != null)
                            mainnetwork_addeditViewModel!!.sendActuatorData(actuator, data)
                    }
                } catch (e: Exception) {
                    ToastHelper.showToast(getString(R.string.set_toast_error))
                }

            }

            override fun sendBooleanFalse() {
                if (actuator != null) {
                    mainnetwork_addeditViewModel!!.sendActuatorData(actuator, "false")
                }
            }

            override fun sendBooleanTrue() {
                if (actuator != null) {
                    mainnetwork_addeditViewModel!!.sendActuatorData(actuator, "true")
                }
            }

            override fun sendString() {
                if (actuator != null) {
                    val data = binding.etActuatorStringValue.text.toString()
                    if (data != null)
                        mainnetwork_addeditViewModel!!.sendActuatorData(actuator, data)
                }
            }
        }

    }

    interface ActuatorMainDashboardListener {

        fun sendInteger()

        fun sendFloat()

        fun sendBooleanFalse()

        fun sendBooleanTrue()

        fun sendString()

    }
}
