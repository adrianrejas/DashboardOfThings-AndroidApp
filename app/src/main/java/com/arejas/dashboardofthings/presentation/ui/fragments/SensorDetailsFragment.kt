package com.arejas.dashboardofthings.presentation.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.FragmentSensorDetailsBinding
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.activities.SensorAddEditActivity
import com.arejas.dashboardofthings.presentation.ui.activities.SensorDetailsActivity
import com.arejas.dashboardofthings.presentation.ui.activities.SensorListActivity
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveSensorDialogFragment
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper

import javax.inject.Inject

import dagger.android.support.AndroidSupportInjection

/**
 * A fragment representing a single Sensor detail screen.
 * This fragment is either contained in a [SensorListActivity]
 * in two-pane mode (on tablets) or a [SensorDetailsActivity]
 * on handsets.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class SensorDetailsFragment : Fragment() {

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    internal var uiBinding: FragmentSensorDetailsBinding

    var sensorId: Int? = null
    var bTwoPane: Boolean = false
    var sensorObject: SensorExtended? = null

    private var sensorDetailsViewModel: SensorDetailsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments!!.containsKey(SENSOR_ID)) {
            sensorId = arguments!!.getInt(SENSOR_ID)
        } else {
            sensorId = null
        }

        if (arguments!!.containsKey(TWO_PANE)) {
            bTwoPane = arguments!!.getBoolean(TWO_PANE)
        } else {
            bTwoPane = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        uiBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_sensor_details, container, false)

        if (!bTwoPane) {
            uiBinding.toolbarFragment.navigationIcon = resources.getDrawable(R.drawable.action_back)
            uiBinding.toolbarFragment.setNavigationOnClickListener { activity!!.finish() }
        } else {
            uiBinding.toolbarFragment.navigationIcon = null
        }

        // Init the view pager with a fragment adapter for showing the fragments with different info
        // of the sensor details in a tab system
        val fragmentAdapter = SensorDetailsFragmentPagerAdapter(
            activity!!.supportFragmentManager,
            context,
            sensorId!!
        )
        uiBinding.vpSensordetailsMaindashboard.adapter = fragmentAdapter
        uiBinding.tlTabsSensordetails.setupWithViewPager(uiBinding.vpSensordetailsMaindashboard)

        uiBinding.toolbarFragment.inflateMenu(R.menu.menu_sensor_management)
        uiBinding.toolbarFragment.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_reload -> sensorDetailsViewModel!!.requestSensorReload()!!.observe(this@SensorDetailsFragment,
                    { resource ->
                        if (resource == null || resource!!.getStatus() == Resource.Status.ERROR) {
                            ToastHelper.showToast(getString(R.string.toast_sensor_reload_request_failed))
                        } else if (resource!!.getStatus() == Resource.Status.SUCCESS) {
                            ToastHelper.showToast(getString(R.string.toast_sensor_reload_request_success))
                        }
                    })
                R.id.menu_edit -> {
                    val intent = Intent(
                        context,
                        SensorAddEditActivity::class.java
                    )
                    intent.putExtra(SensorAddEditActivity.SENSOR_ID, sensorId)
                    startActivity(intent)
                }
                R.id.menu_remove -> if (sensorObject != null) {
                    val dialog = RemoveSensorDialogFragment(sensorObject, sensorDetailsViewModel) {
                        if (bTwoPane) {
                            fragmentManager!!.beginTransaction()
                                .remove(this@SensorDetailsFragment).commit()
                        } else {
                            activity!!.finish()
                        }
                    }
                    dialog.show(activity!!.supportFragmentManager, "removeSensor")
                } else {
                    ToastHelper.showToast(getString(R.string.toast_remove_failed))
                }
            }

            false
        }

        return uiBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Inject dependencies
        AndroidSupportInjection.inject(this)
        // If the sensor ID is defined load the suitable viewmodel and observe the suitable data.
        if (sensorId != null) {
            // Get the viewmodel
            sensorDetailsViewModel = ViewModelProviders.of(activity!!, this.viewModelFactory)
                .get(SensorDetailsViewModel::class.java)
            sensorDetailsViewModel!!.sensorId = sensorId

            sensorDetailsViewModel!!.getSensor(false)!!.observe(this, { sensorExtendedResource ->
                if (sensorExtendedResource == null) {
                    uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_sensor_unrecognized)
                } else {
                    if (sensorExtendedResource!!.getStatus() == Resource.Status.ERROR) {
                        uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_sensor_unrecognized)
                    } else if (sensorExtendedResource!!.getStatus() == Resource.Status.LOADING) {
                        uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_sensor_loading)
                    } else {
                        val sensor = sensorExtendedResource!!.data
                        if (sensor != null) {
                            sensorObject = sensor
                            uiBinding.sensor = sensor
                            uiBinding.toolbarFragment.setTitle(sensor!!.name)
                        } else {
                            uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_sensor_unrecognized)
                        }
                    }
                }
            })
        }
    }

    /**
     * This adapter is used for defining the tab system of the sensor details activity, providing the
     * fragments it will used, so as the tab configuration.
     */
    internal class SensorDetailsFragmentPagerAdapter(
        fragmentManager: FragmentManager,
        private val mContext: Context?,
        private val sensorId: Int
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
            val arguments: Bundle
            val fragment: Fragment
            when (position) {
                0 // Details
                -> {
                    arguments = Bundle()
                    arguments.putInt(SensorDetailsDetailsFragment.SENSOR_ID, sensorId)
                    fragment = SensorDetailsDetailsFragment()
                    fragment.setArguments(arguments)
                    return fragment
                }
                1 // Cast
                -> {
                    arguments = Bundle()
                    arguments.putInt(SensorDetailsLogsFragment.SENSOR_ID, sensorId)
                    fragment = SensorDetailsLogsFragment()
                    fragment.setArguments(arguments)
                    return fragment
                }
                else -> return null
            }
        }

        // Returns the page title for the top indicator
        override fun getPageTitle(position: Int): CharSequence? {
            return if (mContext != null) {
                when (position) {
                    0 // Details
                    -> mContext.getString(R.string.element_details_tab_details)
                    1 // Cast
                    -> mContext.getString(R.string.element_details_tab_logs)
                    else -> null
                }
            } else {
                ""
            }
        }

        companion object {

            private val NUM_ITEMS = 2
        }

    }

    companion object {
        /**
         * The fragment argument representing the item ID that this fragment
         * represents.
         */
        val SENSOR_ID = "sensor_id"
        val TWO_PANE = "two_pane"
    }

}
