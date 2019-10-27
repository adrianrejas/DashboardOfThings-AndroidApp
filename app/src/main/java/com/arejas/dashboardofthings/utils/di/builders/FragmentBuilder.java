package com.arejas.dashboardofthings.utils.di.builders;

import com.arejas.dashboardofthings.presentation.ui.fragments.MainActuatorsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainHistoryFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainLogsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainSensorsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.MainStatusFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailsDetailsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailsLogsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsDetailsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsFragment;
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsLogsFragment;

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
    abstract NetworkDetailsFragment bindNetworkDetailFragment();

    @ContributesAndroidInjector
    abstract NetworkDetailsDetailsFragment bindNetworkDetailDetailsFragment();

    @ContributesAndroidInjector
    abstract NetworkDetailsLogsFragment bindNetworkDetailLogsFragment();

    @ContributesAndroidInjector
    abstract SensorDetailsFragment bindSensorDetailFragment();

    @ContributesAndroidInjector
    abstract SensorDetailsDetailsFragment bindSensorDetailDetailsFragment();

    @ContributesAndroidInjector
    abstract SensorDetailsLogsFragment bindSensorDetailLogsFragment();

}