package com.arejas.dashboardofthings.presentation.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.R
import com.arejas.dashboardofthings.databinding.FragmentNetworkDetailsBinding
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended
import com.arejas.dashboardofthings.domain.entities.result.Resource
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkDetailsViewModel
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkAddEditActivity
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkDetailsActivity
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkListActivity
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveNetworkDialogFragment
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup

import javax.inject.Inject

import dagger.android.support.AndroidSupportInjection

/**
 * A fragment representing a single Network detail screen.
 * This fragment is either contained in a [NetworkListActivity]
 * in two-pane mode (on tablets) or a [NetworkDetailsActivity]
 * on handsets.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
 */
class NetworkDetailsFragment : Fragment() {

    @Inject
    internal var viewModelFactory: ViewModelFactory? = null

    internal var uiBinding: FragmentNetworkDetailsBinding

    var networkId: Int? = null
    var bTwoPane: Boolean = false
    var networkObject: NetworkExtended? = null

    private var networkDetailsViewModel: NetworkDetailsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (arguments!!.containsKey(NETWORK_ID)) {
            networkId = arguments!!.getInt(NETWORK_ID)
        } else {
            networkId = null
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
            DataBindingUtil.inflate(inflater, R.layout.fragment_network_details, container, false)

        if (!bTwoPane) {
            uiBinding.toolbarFragment.navigationIcon = resources.getDrawable(R.drawable.action_back)
            uiBinding.toolbarFragment.setNavigationOnClickListener { activity!!.finish() }
        } else {
            uiBinding.toolbarFragment.navigationIcon = null
        }

        // Init the view pager with a fragment adapter for showing the fragments with different info
        // of the network in a tab system
        val fragmentAdapter = NetworkDetailsFragmentPagerAdapter(
            activity!!.supportFragmentManager,
            context,
            networkId!!
        )
        uiBinding.vpNetworkdetailsMaindashboard.adapter = fragmentAdapter
        uiBinding.tlTabsNetworkdetails.setupWithViewPager(uiBinding.vpNetworkdetailsMaindashboard)

        uiBinding.toolbarFragment.inflateMenu(R.menu.menu_element_management)
        uiBinding.toolbarFragment.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    val intent = Intent(
                        context,
                        NetworkAddEditActivity::class.java
                    )
                    intent.putExtra(NetworkAddEditActivity.NETWORK_ID, networkId)
                    startActivity(intent)
                }
                R.id.menu_remove -> if (networkObject != null) {
                    val dialog =
                        RemoveNetworkDialogFragment(networkObject, networkDetailsViewModel) {
                            if (bTwoPane) {
                                fragmentManager!!.beginTransaction()
                                    .remove(this@NetworkDetailsFragment).commit()
                            } else {
                                activity!!.finish()
                            }
                        }
                    dialog.show(activity!!.supportFragmentManager, "removeNetwork")
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
        // If the network ID is defined load the suitable viewmodel and observe datainterested.
        if (networkId != null) {
            // Get the viewmodel
            networkDetailsViewModel = ViewModelProviders.of(activity!!, this.viewModelFactory)
                .get(NetworkDetailsViewModel::class.java)
            networkDetailsViewModel!!.networkId = networkId

            networkDetailsViewModel!!.getNetwork(false)!!.observe(this, { networkExtendedResource ->
                if (networkExtendedResource == null) {
                    uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_network_unrecognized)
                } else {
                    if (networkExtendedResource!!.getStatus() == Resource.Status.ERROR) {
                        uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_network_unrecognized)
                    } else if (networkExtendedResource!!.getStatus() == Resource.Status.LOADING) {
                        uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_network_loading)
                    } else {
                        val network = networkExtendedResource!!.data
                        if (network != null) {
                            networkObject = network
                            uiBinding.network = network
                            uiBinding.toolbarFragment.setTitle(network!!.name)
                        } else {
                            uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_network_unrecognized)
                        }
                    }
                }
            })
        }
    }

    /**
     * This adapter is used for defining the tab system of the network details activity, providing the
     * fragments it will used, so as the tab configuration.
     */
    internal class NetworkDetailsFragmentPagerAdapter(
        fragmentManager: FragmentManager,
        private val mContext: Context?,
        private val networkId: Int
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
                    arguments.putInt(NetworkDetailsDetailsFragment.NETWORK_ID, networkId)
                    fragment = NetworkDetailsDetailsFragment()
                    fragment.setArguments(arguments)
                    return fragment
                }
                1 // Cast
                -> {
                    arguments = Bundle()
                    arguments.putInt(NetworkDetailsLogsFragment.NETWORK_ID, networkId)
                    fragment = NetworkDetailsLogsFragment()
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
        val NETWORK_ID = "network_id"
        val TWO_PANE = "two_pane"
    }

}
