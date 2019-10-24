package com.arejas.dashboardofthings.utils.di.builders;

import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorListActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.MainDashboardActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.MapActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkAddEditActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkDetailActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkListActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.SensorListActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.SettingsActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuilder {

    @ContributesAndroidInjector
    abstract MainDashboardActivity bindMainDashboardActivity();

    @ContributesAndroidInjector
    abstract NetworkListActivity bindNetworkListActivity();

    @ContributesAndroidInjector
    abstract NetworkDetailActivity bindNetworkDetailActivity();

    @ContributesAndroidInjector
    abstract NetworkAddEditActivity bindNetworkAddEditActivity();

    @ContributesAndroidInjector
    abstract SensorListActivity bindSensorListActivity();

    @ContributesAndroidInjector
    abstract ActuatorListActivity bindActuatorListActivity();

    @ContributesAndroidInjector
    abstract MapActivity bindMapActivity();

    @ContributesAndroidInjector
    abstract SettingsActivity binSettingsActivity();

}