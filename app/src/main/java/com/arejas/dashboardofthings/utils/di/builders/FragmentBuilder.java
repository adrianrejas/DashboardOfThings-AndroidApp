package com.arejas.dashboardofthings.utils.di.builders;

import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorListActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.MainDashboardActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.MapActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkAddEditActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkDetailActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkListActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.SensorListActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.SettingsActivity;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainActuatorsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainHistoryFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainLogsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainSensorsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainStatusFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailDetailsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailLogsFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentBuilder {

    @ContributesAndroidInjector
    abstract MainActuatorsFragment bindMainActuatorsFragment();

    @ContributesAndroidInjector
    abstract MainHistoryFragment bindMainHistoryFragment();

    @ContributesAndroidInjector
    abstract MainLogsFragment bindMainLogsFragment();

    @ContributesAndroidInjector
    abstract MainSensorsFragment bindMainSensorsFragment();

    @ContributesAndroidInjector
    abstract MainStatusFragment bindMainStatusFragment();

    @ContributesAndroidInjector
    abstract NetworkDetailFragment bindNetworkDetailFragment();

    @ContributesAndroidInjector
    abstract NetworkDetailDetailsFragment bindNetworkDetailDetailsFragment();

    @ContributesAndroidInjector
    abstract NetworkDetailLogsFragment bindNetworkDetailLogsFragment();

}