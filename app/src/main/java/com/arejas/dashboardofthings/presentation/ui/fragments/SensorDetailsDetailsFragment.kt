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
import com.arejas.dashboardofthings.databinding.FragmentSensorDetailsDetailsBinding
import com.arejas.dashboardofthings.domain.entities.database.Actuator
import com.arejas.dashboardofthings.domain.entities.database.DataValue
import com.arejas.dashboardofthings.domain.entities.database.Sensor
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.activities.SensorDetailsActivity
import com.arejas.dashboardofthings.presentation.ui.activities.SensorListActivity
import com.arejas.dashboardofthings.presentation.ui.helpers.HistoryChartHelper
import com.arejas.dashboardofthings.presentation.ui.helpers.SensorDetailsListener
import com.arejas.dashboardofthings.utils.Enumerators
import java.util.Objects

import javax.inject.Inject

import dagger.android.support.AndroidSupportInjection

/**
 * A fragment representing a single Sensor detail screen.
 * This fragment is either contained in a [SensorListActivity]
 * in two-pane mode (on tablets) or a [SensorDetailsActivity]
 * on handsets.
 */
class SensorDetailsDetailsFragment : Fragment(), SensorDetailsListener {

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    internal var uiBinding: FragmentSensorDetailsDetailsBinding? = null

    var sensorId: Int? = null

    private var sensorDetailsViewModel: SensorDetailsViewModel? = null

    private var currentInfoShown: LiveData<Resource<SensorExtended>>? = null
    private var lastDataReceived: LiveData<Resource<DataValue>>? = null
    private var lastHistoryDataReceived: LiveData<Resource<List<DataValue>>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments!!.containsKey(SENSOR_ID)) {
            sensorId = arguments!!.getInt(SENSOR_ID)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Inject dependencies
        AndroidSupportInjection.inject(this)
        if (sensorId != null) {
            // Get the sensor details activity view model and observe the changes in the details
            sensorDetailsViewModel = ViewModelProviders.of(
                Objects.requireNonNull<FragmentActivity>(activity),
                viewModelFactory
            ).get(SensorDetailsViewModel::class.java)
            sensorDetailsViewModel!!.sensorId = sensorId
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
            R.layout.fragment_sensor_details_details,
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
        currentInfoShown = sensorDetailsViewModel!!.getSensor(refreshData)
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
        if (lastDataReceived != null) {
            lastDataReceived!!.removeObservers(this)
            lastDataReceived = null
        }
        lastDataReceived = sensorDetailsViewModel!!.getLastValueForSensor(refreshData)
        if (lastDataReceived != null) {
            lastDataReceived!!.observe(this, { resource ->
                if (resource == null) {
                    showError()
                } else {
                    if (resource!!.getStatus() == Resource.Status.SUCCESS && resource!!.data != null) {
                        uiBinding!!.lastValue = resource!!.data!!.value
                    }
                }
            })
        }
        reloadHistoryData(refreshData)
    }

    private fun updateData(sensor: SensorExtended?) {
        if (sensor != null) {
            showData()
            uiBinding!!.sensor = sensor
            uiBinding!!.sensor = sensor
            if (sensor.dataType == Enumerators.DataType.STRING) {
                uiBinding!!.spSensorDetailsHistorySpinner.visibility = View.GONE
                uiBinding!!.lcSensorDetailsHistoryChart.visibility = View.GONE
            } else if (sensor.dataType == Enumerators.DataType.BOOLEAN) {
                uiBinding!!.historySpinnerSelected = HistoryChartHelper.SPINNER_HISTORY_LASTVAL
                uiBinding!!.spSensorDetailsHistorySpinner.setSelection(HistoryChartHelper.SPINNER_HISTORY_LASTVAL)
                uiBinding!!.spSensorDetailsHistorySpinner.visibility = View.GONE
            } else {
                uiBinding!!.historySpinnerSelected = sensorDetailsViewModel!!.historySpinnerPosition
                uiBinding!!.spSensorDetailsHistorySpinner.setSelection(sensorDetailsViewModel!!.historySpinnerPosition)
                uiBinding!!.spSensorDetailsHistorySpinner.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Show an error on the data loading. More info will be shown on the logs
     */
    private fun showError() {
        if (uiBinding != null) {
            uiBinding!!.sensorDetailsLayout.visibility = View.GONE
            uiBinding!!.sensorDetailsLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.sensorDetailsErrorLayout.errorLayout.visibility = View.VISIBLE
            uiBinding!!.sensorDetailsErrorLayout.tvError.text = getString(R.string.error_in_list)
        }
    }

    /**
     * Show the fragment info is loading.
     */
    private fun showLoading() {
        if (uiBinding != null) {
            uiBinding!!.sensorDetailsLayout.visibility = View.GONE
            uiBinding!!.sensorDetailsLoadingLayout.loadingLayout.visibility = View.VISIBLE
            uiBinding!!.sensorDetailsErrorLayout.errorLayout.visibility = View.GONE
        }
    }

    /**
     * Show the fragment info with data.
     */
    private fun showData() {
        if (uiBinding != null) {
            uiBinding!!.sensorDetailsLayout.visibility = View.VISIBLE
            uiBinding!!.sensorDetailsLoadingLayout.loadingLayout.visibility = View.GONE
            uiBinding!!.sensorDetailsErrorLayout.errorLayout.visibility = View.GONE
        }
    }

    private fun reloadHistoryData(refreshData: Boolean) {
        if (lastHistoryDataReceived != null)
            lastHistoryDataReceived!!.removeObservers(this)
        lastHistoryDataReceived = sensorDetailsViewModel!!.getHistoricalData(refreshData)
        lastHistoryDataReceived!!.observe(this, { listResource ->
            if (listResource == null) {
                showError()
            } else {
                if (listResource!!.getStatus() == Resource.Status.ERROR) {
                    showError()
                } else if (listResource!!.getStatus() == Resource.Status.LOADING) {
                    showLoading()
                } else {
                    showData()
                    uiBinding!!.historySpinnerSelected =
                        sensorDetailsViewModel!!.historySpinnerPosition
                    uiBinding!!.historyData = listResource!!.data
                }
            }
        })
    }

    override fun onSpinnerItemSelected(position: Int) {
        if (sensorId != null) {
            sensorDetailsViewModel!!.historySpinnerPosition = position
            reloadHistoryData(false)
        }
    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        val SENSOR_ID = "sensor_id"
    }

}
