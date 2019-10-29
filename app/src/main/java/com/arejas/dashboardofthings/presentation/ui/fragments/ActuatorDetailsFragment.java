package com.arejas.dashboardofthings.presentation.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.R;
import com.arejas.dashboardofthings.databinding.FragmentActuatorDetailsBinding;
import com.arejas.dashboardofthings.domain.entities.extended.ActuatorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.ActuatorDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorAddEditActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorDetailsActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorListActivity;
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveActuatorDialogFragment;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * A fragment representing a single Actuator detail screen.
 * This fragment is either contained in a {@link ActuatorListActivity}
 * in two-pane mode (on tablets) or a {@link ActuatorDetailsActivity}
 * on handsets.
 */
public class ActuatorDetailsFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ACTUATOR_ID = "actuator_id";
    public static final String TWO_PANE = "two_pane";

    @Inject
    ViewModelFactory viewModelFactory;

    FragmentActuatorDetailsBinding uiBinding;

    public Integer actuatorId;
    public boolean bTwoPane;
    public ActuatorExtended actuatorObject;

    private ActuatorDetailsViewModel actuatorDetailsViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ActuatorDetailsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ACTUATOR_ID)) {
            actuatorId = getArguments().getInt(ACTUATOR_ID);
        } else {
            actuatorId = null;
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
        uiBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_actuator_details, container, false);

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
        // of the actuator details in a tab system
        ActuatorDetailsFragmentPagerAdapter fragmentAdapter = new ActuatorDetailsFragmentPagerAdapter(getActivity().getSupportFragmentManager(), getContext(), actuatorId);
        uiBinding.vpActuatordetailsMaindashboard.setAdapter(fragmentAdapter);
        uiBinding.tlTabsActuatordetails.setupWithViewPager(uiBinding.vpActuatordetailsMaindashboard);

        uiBinding.toolbarFragment.inflateMenu(R.menu.menu_element_management);
        uiBinding.toolbarFragment.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menu_edit:
                        Intent intent = new Intent(DotApplication.getContext(),
                                ActuatorAddEditActivity.class);
                        intent.putExtra(ActuatorAddEditActivity.ACTUATOR_ID, actuatorId);
                        DotApplication.getContext().startActivity(intent);
                        break;
                    case R.id.menu_remove:
                        if (actuatorObject != null) {
                            RemoveActuatorDialogFragment dialog =
                                    new RemoveActuatorDialogFragment(actuatorObject, actuatorDetailsViewModel,() -> {
                                        if (bTwoPane) {
                                            getFragmentManager().beginTransaction()
                                                    .remove(ActuatorDetailsFragment.this).commit();
                                        } else {
                                            getActivity().finish();
                                        }
                                    });
                            dialog.show(getActivity().getSupportFragmentManager(), "removeActuator");
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
        // If the actuator ID is defined load the suitable viewmodel and observe the suitable data.
        if (actuatorId != null) {
            // Get the viewmodel
            actuatorDetailsViewModel = ViewModelProviders.of(getActivity(), this.viewModelFactory).get(ActuatorDetailsViewModel.class);
            actuatorDetailsViewModel.setActuatorId(actuatorId);

            actuatorDetailsViewModel.getActuator(false).observe(this, actuatorExtendedResource -> {
                if (actuatorExtendedResource == null) {
                    uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_actuator_unrecognized);
                } else {
                    if (actuatorExtendedResource.getStatus() == Resource.Status.ERROR) {
                        uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_actuator_unrecognized);
                    } else if (actuatorExtendedResource.getStatus() == Resource.Status.LOADING) {
                        uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_actuator_loading);
                    } else {
                        ActuatorExtended actuator = actuatorExtendedResource.getData();
                        if (actuator != null) {
                            actuatorObject = actuator;
                            uiBinding.setActuator(actuator);
                            uiBinding.toolbarFragment.setTitle(actuator.getName());
                        } else {
                            uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_actuator_unrecognized);
                        }
                    }
                }
            });
        }
    }

    /**
     * This adapter is used for defining the tab system of the actuator details activity, providing the
     * fragments it will used, so as the tab configuration.
     */
    static class ActuatorDetailsFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private static final int NUM_ITEMS = 2;

        private final Context mContext;

        private final int actuatorId;

        ActuatorDetailsFragmentPagerAdapter(FragmentManager fragmentManager, Context context, int actuatorId) {
            super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.mContext = context;
            this.actuatorId = actuatorId;
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
                    arguments.putInt(ActuatorDetailsDetailsFragment.ACTUATOR_ID, actuatorId);
                    fragment = new ActuatorDetailsDetailsFragment();
                    fragment.setArguments(arguments);
                    return fragment;
                case 1: // Cast
                    arguments = new Bundle();
                    arguments.putInt(ActuatorDetailsLogsFragment.ACTUATOR_ID, actuatorId);
                    fragment = new ActuatorDetailsLogsFragment();
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
