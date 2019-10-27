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
import com.arejas.dashboardofthings.databinding.FragmentSensorDetailsBinding;
import com.arejas.dashboardofthings.domain.entities.extended.SensorExtended;
import com.arejas.dashboardofthings.domain.entities.result.Resource;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.SensorDetailsViewModel;
import com.arejas.dashboardofthings.presentation.interfaces.viewmodels.factories.ViewModelFactory;
import com.arejas.dashboardofthings.presentation.ui.activities.SensorAddEditActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.SensorDetailsActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.SensorListActivity;
import com.arejas.dashboardofthings.presentation.ui.notifications.RemoveSensorDialogFragment;
import com.arejas.dashboardofthings.presentation.ui.notifications.ToastHelper;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

/**
 * A fragment representing a single Sensor detail screen.
 * This fragment is either contained in a {@link SensorListActivity}
 * in two-pane mode (on tablets) or a {@link SensorDetailsActivity}
 * on handsets.
 */
public class SensorDetailsFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String SENSOR_ID = "sensor_id";
    public static final String TWO_PANE = "two_pane";

    @Inject
    ViewModelFactory viewModelFactory;

    FragmentSensorDetailsBinding uiBinding;

    public Integer sensorId;
    public boolean bTwoPane;
    public SensorExtended sensorObject;

    private SensorDetailsViewModel sensorDetailsViewModel;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SensorDetailsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(SENSOR_ID)) {
            sensorId = getArguments().getInt(SENSOR_ID);
        } else {
            sensorId = null;
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
        uiBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sensor_details, container, false);

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
        SensorDetailsFragmentPagerAdapter fragmentAdapter = new SensorDetailsFragmentPagerAdapter(getActivity().getSupportFragmentManager(), getContext(), sensorId);
        uiBinding.vpSensordetailsMaindashboard.setAdapter(fragmentAdapter);
        uiBinding.tlTabsSensordetails.setupWithViewPager(uiBinding.vpSensordetailsMaindashboard);

        uiBinding.toolbarFragment.inflateMenu(R.menu.menu_sensor_management);
        uiBinding.toolbarFragment.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {

            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menu_reload:
                        sensorDetailsViewModel.requestSensorReload().observe(SensorDetailsFragment.this,
                                resource -> {
                                    if ((resource == null) ||
                                            (resource.getStatus().equals(Resource.Status.ERROR))) {
                                        ToastHelper.showToast(getString(R.string.toast_sensor_reload_request_failed));
                                    } else if (resource.getStatus().equals(Resource.Status.SUCCESS)) {
                                        ToastHelper.showToast(getString(R.string.toast_sensor_reload_request_success));
                                    }
                                });
                        break;
                    case R.id.menu_edit:
                        Intent intent = new Intent(DotApplication.getContext(),
                                SensorAddEditActivity.class);
                        intent.putExtra(SensorAddEditActivity.SENSOR_ID, sensorId);
                        DotApplication.getContext().startActivity(intent);
                        break;
                    case R.id.menu_remove:
                        if (sensorObject != null) {
                            RemoveSensorDialogFragment dialog =
                                    new RemoveSensorDialogFragment(sensorObject, sensorDetailsViewModel,() -> {
                                        if (bTwoPane) {
                                            getFragmentManager().beginTransaction()
                                                    .remove(SensorDetailsFragment.this).commit();
                                        } else {
                                            getActivity().finish();
                                        }
                                    });
                            dialog.show(getActivity().getSupportFragmentManager(), "removeSensor");
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
        if (sensorId != null) {
            // Get the viewmodel
            sensorDetailsViewModel = ViewModelProviders.of(getActivity(), this.viewModelFactory).get(SensorDetailsViewModel.class);
            sensorDetailsViewModel.setSensorId(sensorId);

            sensorDetailsViewModel.getSensor(false).observe(this, sensorExtendedResource -> {
                if (sensorExtendedResource == null) {
                    uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_sensor_unrecognized);
                } else {
                    if (sensorExtendedResource.getStatus() == Resource.Status.ERROR) {
                        uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_sensor_unrecognized);
                    } else if (sensorExtendedResource.getStatus() == Resource.Status.LOADING) {
                        uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_sensor_loading);
                    } else {
                        SensorExtended sensor = sensorExtendedResource.getData();
                        if (sensor != null) {
                            sensorObject = sensor;
                            uiBinding.setSensor(sensor);
                            uiBinding.toolbarFragment.setTitle(sensor.getName());
                        } else {
                            uiBinding.toolbarFragment.setTitle(R.string.toolbar_title_sensor_unrecognized);
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
    static class SensorDetailsFragmentPagerAdapter extends FragmentStatePagerAdapter {

        private static final int NUM_ITEMS = 2;

        private final Context mContext;

        private final int sensorId;

        SensorDetailsFragmentPagerAdapter(FragmentManager fragmentManager, Context context, int sensorId) {
            super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.mContext = context;
            this.sensorId = sensorId;
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
                    arguments.putInt(SensorDetailsDetailsFragment.SENSOR_ID, sensorId);
                    fragment = new SensorDetailsDetailsFragment();
                    fragment.setArguments(arguments);
                    return fragment;
                case 1: // Cast
                    arguments = new Bundle();
                    arguments.putInt(SensorDetailsLogsFragment.SENSOR_ID, sensorId);
                    fragment = new SensorDetailsLogsFragment();
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
