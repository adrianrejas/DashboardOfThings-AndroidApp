package com.arejas.dashboardofthings.presentation.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders

import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.FragmentActuatorDetailsDetailsBinding
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorDetailsActivity
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorListActivity
import com.arejas.dashboardofthings.presentation.ui.helpers.HistoryChartHelper
import com.arejas.dashboardofthings.presentation.ui.helpers.ActuatorDetailsListener
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper
import com.arejas.dashboardofthings.utils.Enumerators
import java.util.Objects

import javax.inject.Inject

import dagger.android.support.AndroidSupportInjection

/**
 * A fragment representing a single Actuator detail screen.
 * This fragment is either contained in a [ActuatorListActivity]
 * in two-pane mode (on tablets) or a [ActuatorDetailsActivity]
 * on handsets.
 */
class ActuatorDetailsDetailsFragment : Fragment(), ActuatorDetailsListener {

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    internal var uiBinding: FragmentActuatorDetailsDetailsBinding? = null

    var actuatorId: Int? = null

    private var actuatorDetailsViewModel: ActuatorDetailsViewModel? = null

    private var currentInfoShown: LiveData<Resource<ActuatorExtended>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments!!.containsKey(ACTUATOR_ID)) {
            actuatorId = arguments!!.getInt(ACTUATOR_ID)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Inject dependencies
        AndroidSupportInjection.inject(this)
        if (actuatorId != null) {
            // Get the actuator details activity view model and observe the changes in the details
            actuatorDetailsViewModel = ViewModelProviders.of(
                Objects.requireNonNull<FragmentActivity>(activity),
                viewModelFactory
            ).get(ActuatorDetailsViewModel::class.java)
            actuatorDetailsViewModel!!.actuatorId = actuatorId
            setData(true, false)
        } else {
            showError()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uiBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_actuator_details_details,
            container,
            false
        )
        uiBinding!!.presenter = this
        // Set action of refreshing list when refreshing gesture detected
        uiBinding!!.srlRefreshLayout.setOnRefreshListener { setData(false, true) }
        return uiBinding!!.root
    }

    /**
     * Function called for setting new data, requesting it to the Database
     *
     * @param showLoading true if wanted to show the loading layout until info got (we don't want in
     * case of swipe refresh, because it has it's own way to info about the loading process).
     * @param refreshData true if wanted to reload the data, false if it's enough with cached data.
     */
    private fun setData(showLoading: Boolean, refreshData: Boolean) {
        if (showLoading) {
            showLoading()
        }
        if (currentInfoShown != null) {
            currentInfoShown!!.removeObservers(this)
            currentInfoShown = null
        }
        currentInfoShown = actuatorDetailsViewModel!!.getActuator(refreshData)
        if (currentInfoShown != null) {
            currentInfoShown!!.observe(this, { listResource ->
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
                        updateData(listResource!!.data)
                        uiBinding!!.srlRefreshLayout.isRefreshing = false
                    }
                }
            })
        }
    }

    private fun updateData(actuator: ActuatorExtended?) {
        if (actuator != null) {
            showData()
            uiBinding!!.actuator = actuator
        }
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private fun showError() {
        if (uiBinding != null) {
            uiBinding!!.actuatorDetailsLayout.visibility = View.GONE
            uiBinding!!.actuatorDetailsLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.actuatorDetailsErrorLayout.errorLayout.visibility = View.VISIBLE
            uiBinding!!.actuatorDetailsErrorLayout.tvError.text = getString(R.string.error_in_list)
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private fun showLoading() {
        if (uiBinding != null) {
            uiBinding!!.actuatorDetailsLayout.visibility = View.GONE
            uiBinding!!.actuatorDetailsLoadingLayout.loadingLayout.visibility = View.VISIBLE
            uiBinding!!.actuatorDetailsErrorLayout.errorLayout.visibility = View.GONE
        }
    }

    /**
     * Show the fragment info with data.
     */
    private fun showData() {
        if (uiBinding != null) {
            uiBinding!!.actuatorDetailsLayout.visibility = View.VISIBLE
            uiBinding!!.actuatorDetailsLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.actuatorDetailsErrorLayout.errorLayout.visibility = View.GONE
        }
    }

    override fun sendInteger() {
        try {
            if (currentInfoShown != null && currentInfoShown!!.value != null &&
                currentInfoShown!!.value!!.status == Resource.Status.SUCCESS
            ) {
                val data = uiBinding!!.etActuatorDetailIntegerValue.text.toString()
                val actuator = currentInfoShown!!.value!!.data
                val comparingData = java.lang.Float.valueOf(data)
                if (actuator!!.dataNumberMinimum != null && actuator.dataNumberMaximum != null) {
                    if (comparingData < actuator.dataNumberMinimum || comparingData > actuator.dataNumberMaximum) {
                        ToastHelper.showToast(
                            getString(
                                R.string.set_toast_between,
                                actuator.dataNumberMinimum!!.toString(),
                                actuator.dataNumberMaximum!!.toString()
                            )
                        )
                        return
                    }
                } else if (actuator.dataNumberMinimum != null) {
                    if (comparingData < actuator.dataNumberMinimum) {
                        ToastHelper.showToast(
                            getString(
                                R.string.set_toast_smaller_than,
                                actuator.dataNumberMinimum!!.toString()
                            )
                        )
                        return
                    }
                } else if (actuator.dataNumberMaximum != null) {
                    if (comparingData > actuator.dataNumberMaximum) {
                        ToastHelper.showToast(
                            getString(
                                R.string.set_toast_bigger_than,
                                actuator.dataNumberMaximum!!.toString()
                            )
                        )
                        return
                    }
                }
                if (data != null)
                    actuatorDetailsViewModel!!.sendActuatorData(data)
            }
        } catch (e: Exception) {
            ToastHelper.showToast(getString(R.string.set_toast_error))
        }

    }

    override fun sendFloat() {
        try {
            if (currentInfoShown != null && currentInfoShown!!.value != null &&
                currentInfoShown!!.value!!.status == Resource.Status.SUCCESS
            ) {
                val data = uiBinding!!.etActuatorDetailDecimalValue.text.toString()
                val actuator = currentInfoShown!!.value!!.data
                val comparingData = java.lang.Float.valueOf(data)
                if (actuator!!.dataNumberMinimum != null && actuator.dataNumberMaximum != null) {
                    if (comparingData < actuator.dataNumberMinimum || comparingData > actuator.dataNumberMaximum) {
                        ToastHelper.showToast(
                            getString(
                                R.string.set_toast_between,
                                actuator.dataNumberMinimum!!.toString(),
                                actuator.dataNumberMaximum!!.toString()
                            )
                        )
                        return
                    }
                } else if (actuator.dataNumberMinimum != null) {
                    if (comparingData < actuator.dataNumberMinimum) {
                        ToastHelper.showToast(
                            getString(
                                R.string.set_toast_smaller_than,
                                actuator.dataNumberMinimum!!.toString()
                            )
                        )
                        return
                    }
                } else if (actuator.dataNumberMaximum != null) {
                    if (comparingData > actuator.dataNumberMaximum) {
                        ToastHelper.showToast(
                            getString(
                                R.string.set_toast_bigger_than,
                                actuator.dataNumberMaximum!!.toString()
                            )
                        )
                        return
                    }
                }
                if (data != null)
                    actuatorDetailsViewModel!!.sendActuatorData(data)
            }
        } catch (e: Exception) {
            ToastHelper.showToast(getString(R.string.set_toast_error))
        }

    }

    override fun sendBooleanFalse() {
        if (currentInfoShown != null && currentInfoShown!!.value != null &&
            currentInfoShown!!.value!!.status == Resource.Status.SUCCESS
        ) {
            actuatorDetailsViewModel!!.sendActuatorData("false")
        }
    }

    override fun sendBooleanTrue() {
        if (currentInfoShown != null && currentInfoShown!!.value != null &&
            currentInfoShown!!.value!!.status == Resource.Status.SUCCESS
        ) {
            actuatorDetailsViewModel!!.sendActuatorData("true")
        }
    }

    override fun sendString() {
        if (currentInfoShown != null && currentInfoShown!!.value != null &&
            currentInfoShown!!.value!!.status == Resource.Status.SUCCESS
        ) {
            val data = uiBinding!!.etActuatorDetailStringValue.text.toString()
            if (data != null)
                actuatorDetailsViewModel!!.sendActuatorData(data)
        }
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        val ACTUATOR_ID = "actuator_id"
    }

}
