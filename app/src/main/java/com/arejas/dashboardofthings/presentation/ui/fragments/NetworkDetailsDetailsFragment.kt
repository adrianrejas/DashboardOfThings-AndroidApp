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
import com.arejas.dashboardofthings.databinding.FragmentNetworkDetailsDetailsBinding
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkDetailsActivity
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkListActivity
import java.util.Objects

import javax.inject.Inject

import dagger.android.support.AndroidSupportInjection

/**
 * A fragment representing a single Network detail screen.
 * This fragment is either contained in a [NetworkListActivity]
 * in two-pane mode (on tablets) or a [NetworkDetailsActivity]
 * on handsets.
 */
class NetworkDetailsDetailsFragment : Fragment() {

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    internal var uiBinding: FragmentNetworkDetailsDetailsBinding? = null

    var networkId: Int? = null

    private var networkDetailsViewModel: NetworkDetailsViewModel? = null

    private var currentInfoShown: LiveData<Resource<NetworkExtended>>? = null
    private var currentRelatedSensors: LiveData<Resource<List<Sensor>>>? = null
    private var currentRelatedActuators: LiveData<Resource<List<Actuator>>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments!!.containsKey(NETWORK_ID)) {
            networkId = arguments!!.getInt(NETWORK_ID)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Inject dependencies
        AndroidSupportInjection.inject(this)
        if (networkId != null) {
            // Get the network details activity view model and observe the changes in the details
            networkDetailsViewModel = ViewModelProviders.of(
                Objects.requireNonNull<FragmentActivity>(activity),
                viewModelFactory
            ).get(NetworkDetailsViewModel::class.java)
            networkDetailsViewModel!!.networkId = networkId
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
            R.layout.fragment_network_details_details,
            container,
            false
        )
        // Set action of refreshing list when refreshing gesture detected
        uiBinding!!.srlRefreshLayout.setOnRefreshListener { setData(false, true) }
        return uiBinding!!.root
    }

    /**
     * Function called for setting a new list, requesting it to the Database
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
        currentInfoShown = networkDetailsViewModel!!.getNetwork(refreshData)
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
        if (currentRelatedSensors != null) {
            currentRelatedSensors!!.removeObservers(this)
            currentRelatedSensors = null
        }
        currentRelatedSensors = networkDetailsViewModel!!.getSensorsRelated(refreshData)
        if (currentRelatedSensors != null) {
            currentRelatedSensors!!.observe(this, { listResource ->
                if (listResource == null) {
                    showError()
                } else {
                    if (listResource!!.getStatus() == Resource.Status.SUCCESS && listResource!!.data != null) {
                        updateRelatedSensors(listResource!!.data)
                    }
                }
            })
        }
        if (currentRelatedActuators != null) {
            currentRelatedActuators!!.removeObservers(this)
            currentRelatedActuators = null
        }
        currentRelatedActuators = networkDetailsViewModel!!.getActuatorsRelated(refreshData)
        if (currentRelatedActuators != null) {
            currentRelatedActuators!!.observe(this, { listResource ->
                if (listResource == null) {
                    showError()
                } else {
                    if (listResource!!.getStatus() == Resource.Status.SUCCESS && listResource!!.data != null) {
                        updateRelatedActuators(listResource!!.data)
                    }
                }
            })
        }
    }

    private fun updateData(network: NetworkExtended?) {
        if (network != null) {
            showData()
            uiBinding!!.network = network
        }
    }

    private fun updateRelatedSensors(sensors: List<Sensor>?) {
        if (sensors != null) {
            uiBinding!!.sensorList = sensors
        }
    }

    private fun updateRelatedActuators(actuators: List<Actuator>?) {
        if (actuators != null) {
            uiBinding!!.actuatorList = actuators
        }
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private fun showError() {
        if (uiBinding != null) {
            uiBinding!!.networkDetailsLayout.visibility = View.GONE
            uiBinding!!.networkDetailsLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.networkDetailsErrorLayout.errorLayout.visibility = View.VISIBLE
            uiBinding!!.networkDetailsErrorLayout.tvError.text = getString(R.string.error_in_list)
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private fun showLoading() {
        if (uiBinding != null) {
            uiBinding!!.networkDetailsLayout.visibility = View.GONE
            uiBinding!!.networkDetailsLoadingLayout.loadingLayout.visibility = View.VISIBLE
            uiBinding!!.networkDetailsErrorLayout.errorLayout.visibility = View.GONE
        }
    }

    /**
     * Show the fragment info with data.
     */
    private fun showData() {
        if (uiBinding != null) {
            uiBinding!!.networkDetailsLayout.visibility = View.VISIBLE
            uiBinding!!.networkDetailsLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.networkDetailsErrorLayout.errorLayout.visibility = View.GONE
        }
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        val NETWORK_ID = "network_id"
    }

}
