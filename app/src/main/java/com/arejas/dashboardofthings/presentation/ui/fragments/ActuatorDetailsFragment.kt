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
import com.arejas.dashboardofthings.databinding.FragmentActuatorDetailsBinding
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorAddEditActivity
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorDetailsActivity
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorListActivity
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveActuatorDialogFragment
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper

import javax.inject.Inject

import dagger.android.support.AndroidSupportInjection

/**
 * A fragment representing a single Actuator detail screen.
 * This fragment is either contained in a [ActuatorListActivity]
 * in two-pane mode (on tablets) or a [ActuatorDetailsActivity]
 * on handsets.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class ActuatorDetailsFragment : Fragment() {

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    internal var uiBinding: FragmentActuatorDetailsBinding

    var actuatorId: Int? = null
    var bTwoPane: Boolean = false
    var actuatorObject: ActuatorExtended? = null

    private var actuatorDetailsViewModel: ActuatorDetailsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments!!.containsKey(ACTUATOR_ID)) {
            actuatorId = arguments!!.getInt(ACTUATOR_ID)
        } else {
            actuatorId = null
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
            DataBindingUtil.inflate(inflater, R.layout.fragment_actuator_details, container, false)

        if (!bTwoPane) {
            uiBinding.toolbarFragment.navigationIcon = resources.getDrawable(R.drawable.action_back)
            uiBinding.toolbarFragment.setNavigationOnClickListener { activity!!.finish() }
        } else {
            uiBinding.toolbarFragment.navigationIcon = null
        }

        // Init the view pager with a fragment adapter for showing the fragments with different info
        // of the actuator details in a tab system
        val fragmentAdapter = ActuatorDetailsFragmentPagerAdapter(
            activity!!.supportFragmentManager,
            context,
            actuatorId!!
        )
        uiBinding.vpActuatordetailsMaindashboard.adapter = fragmentAdapter
        uiBinding.tlTabsActuatordetails.setupWithViewPager(uiBinding.vpActuatordetailsMaindashboard)

        uiBinding.toolbarFragment.inflateMenu(R.menu.menu_element_management)
        uiBinding.toolbarFragment.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    val intent = Intent(
                        context,
                        ActuatorAddEditActivity::class.java
                    )
                    intent.putExtra(ActuatorAddEditActivity.ACTUATOR_ID, actuatorId)
                    startActivity(intent)
                }
                R.id.menu_remove -> if (actuatorObject != null) {
                    val dialog =
                        RemoveActuatorDialogFragment(actuatorObject, actuatorDetailsViewModel) {
                            if (bTwoPane) {
                                fragmentManager!!.beginTransaction()
                                    .remove(this@ActuatorDetailsFragment).commit()
                            } else {
                                activity!!.finish()
                            }
                        }
                    dialog.show(activity!!.supportFragmentManager, "removeActuator")
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
        // If the actuator ID is defined load the suitable viewmodel and observe the suitable data.
        if (actuatorId != null) {
            // Get the viewmodel
            actuatorDetailsViewModel = ViewModelProviders.of(activity!!, this.viewModelFactory)
                .get(ActuatorDetailsViewModel::class.java)
            actuatorDetailsViewModel!!.actuatorId = actuatorId

            actuatorDetailsViewModel!!.getActuator(false)!!.observe(
                this,
                { actuatorExtendedResource ->
                    if (actuatorExtendedResource == null) {
                        uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_actuator_unrecognized)
                    } else {
                        if (actuatorExtendedResource!!.getStatus() == Resource.Status.ERROR) {
                            uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_actuator_unrecognized)
                        } else if (actuatorExtendedResource!!.getStatus() == Resource.Status.LOADING) {
                            uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_actuator_loading)
                        } else {
                            val actuator = actuatorExtendedResource!!.data
                            if (actuator != null) {
                                actuatorObject = actuator
                                uiBinding.actuator = actuator
                                uiBinding.toolbarFragment.setTitle(actuator!!.name)
                            } else {
                                uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_actuator_unrecognized)
                            }
                        }
                    }
                })
        }
    }

    /**
     * This adapter is used for defining the tab system of the actuator details activity, providing the
     * fragments it will used, so as the tab configuration.
     */
    internal class ActuatorDetailsFragmentPagerAdapter(
        fragmentManager: FragmentManager,
        private val mContext: Context?,
        private val actuatorId: Int
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
                    arguments.putInt(ActuatorDetailsDetailsFragment.ACTUATOR_ID, actuatorId)
                    fragment = ActuatorDetailsDetailsFragment()
                    fragment.setArguments(arguments)
                    return fragment
                }
                1 // Cast
                -> {
                    arguments = Bundle()
                    arguments.putInt(ActuatorDetailsLogsFragment.ACTUATOR_ID, actuatorId)
                    fragment = ActuatorDetailsLogsFragment()
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
        val ACTUATOR_ID = "actuator_id"
        val TWO_PANE = "two_pane"
    }

}
