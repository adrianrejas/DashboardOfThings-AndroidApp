package com.arejas.dashboardofthings.presentation.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.FragmentNetworkDetailsBinding;
import com.arejas.dashboardofthings.domain.entities.extended.NetworkExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.NetworkDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkAddEditActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkDetailsActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkListActivity;
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveNetworkDialogFragment;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * A fragment representing a single Network detail screen.
 * This fragment is either contained in a {@link NetworkListActivity}
 * in two-pane mode (on tablets) or a {@link NetworkDetailsActivity}
 * on handsets.
 */
public class NetworkDetailsFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String NETWORK_ID = "network_id";
    public static final String TWO_PANE = "two_pane";

    @Inject
    ViewModelFactory viewModelFactory;

    FragmentNetworkDetailsBinding uiBinding;

    public Integer networkId;
    public boolean bTwoPane;
    public NetworkExtended networkObject;

    private NetworkDetailsViewModel networkDetailsViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NetworkDetailsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(NETWORK_ID)) {
            networkId = getArguments().getInt(NETWORK_ID);
        } else {
            networkId = null;
        }

        if (getArguments().containsKey(TWO_PANE)) {
            bTwoPane = getArguments().getBoolean(TWO_PANE);
        } else {
            bTwoPane = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        uiBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_network_details, container, false);

        if (!bTwoPane) {
            uiBinding.toolbarFragment.setNavigationIcon(getResources().getDrawable(R.drawable.action_back));
            uiBinding.toolbarFragment.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        } else {
            uiBinding.toolbarFragment.setNavigationIcon(null);
        }

        // Init the view pager with a fragment adapter for showing the fragments with different info
        // of the movie in a tab system
        NetworkDetailsFragmentPagerAdapter fragmentAdapter = new NetworkDetailsFragmentPagerAdapter(getActivity().getSupportFragmentManager(), getContext(), networkId);
        uiBinding.vpNetworkdetailsMaindashboard.setAdapter(fragmentAdapter);
        uiBinding.tlTabsNetworkdetails.setupWithViewPager(uiBinding.vpNetworkdetailsMaindashboard);

        uiBinding.toolbarFragment.inflateMenu(R.menu.menu_element_management);
        uiBinding.toolbarFragment.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menu_edit:
                        Intent intent = new Intent(DotApplication.getContext(),
                                NetworkAddEditActivity.class);
                        intent.putExtra(NetworkAddEditActivity.NETWORK_ID, networkId);
                        DotApplication.getContext().startActivity(intent);
                        break;
                    case R.id.menu_remove:
                        if (networkObject != null) {
                            RemoveNetworkDialogFragment dialog =
                                    new RemoveNetworkDialogFragment(networkObject, networkDetailsViewModel,() -> {
                                        if (bTwoPane) {
                                            getFragmentManager().beginTransaction()
                                                    .remove(NetworkDetailsFragment.this).commit();
                                        } else {
                                            getActivity().finish();
                                        }
                                    });
                            dialog.show(getActivity().getSupportFragmentManager(), "removeNetwork");
                        } else {
                            ToastHelper.showToast(getString(R.string.toast_remove_failed));
                        }
                        break;
                }

                return false;
            }
        });

        return uiBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Inject dependencies
        AndroidSupportInjection.inject(this);
        // If the recipe ID is defined load the suitable viewmodel.
        if (networkId != null) {
            // Get the viewmodel
            networkDetailsViewModel = ViewModelProviders.of(getActivity(), this.viewModelFactory).get(NetworkDetailsViewModel.class);
            networkDetailsViewModel.setNetworkId(networkId);

            networkDetailsViewModel.getNetwork(false).observe(this, networkExtendedResource -> {
                if (networkExtendedResource == null) {
                    uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_network_unrecognized);
                } else {
                    if (networkExtendedResource.getStatus() == Resource.Status.ERROR) {
                        uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_network_unrecognized);
                    } else if (networkExtendedResource.getStatus() == Resource.Status.LOADING) {
                        uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_network_loading);
                    } else {
                        NetworkExtended network = networkExtendedResource.getData();
                        if (network != null) {
                            networkObject = network;
                            uiBinding.setNetwork(network);
                            uiBinding.toolbarFragment.setTitle(network.getName());
                        } else {
                            uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_network_unrecognized);
                        }
                    }
                }
            });
        }
    }

    /**
     * This adapter is used for defining the tab system of the movie activity, providing the
     * fragments it will used, so as the tab configuration.
     */
    static class NetworkDetailsFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private static final int NUM_ITEMS = 2;

        private final Context mContext;

        private final int networkId;

        NetworkDetailsFragmentPagerAdapter(FragmentManager fragmentManager, Context context, int networkId) {
            super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.mContext = context;
            this.networkId = networkId;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            Bundle arguments;
            Fragment fragment;
            switch (position) {
                case 0: // Details
                    arguments = new Bundle();
                    arguments.putInt(NetworkDetailsDetailsFragment.NETWORK_ID, networkId);
                    fragment = new NetworkDetailsDetailsFragment();
                    fragment.setArguments(arguments);
                    return fragment;
                case 1: // Cast
                    arguments = new Bundle();
                    arguments.putInt(NetworkDetailsLogsFragment.NETWORK_ID, networkId);
                    fragment = new NetworkDetailsLogsFragment();
                    fragment.setArguments(arguments);
                    return fragment;
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            if (mContext != null) {
                switch (position) {
                    case 0: // Details
                        return mContext.getString(R.string.element_details_tab_details);
                    case 1: // Cast
                        return mContext.getString(R.string.element_details_tab_logs);
                    default:
                        return null;
                }
            } else {
                return "";
            }
        }

    }

}
