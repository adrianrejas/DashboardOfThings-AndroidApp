package com.arejas.dashboardofthings.utils.di.builders;

import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorAddEditActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorDetailsActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorListActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.MainDashboardActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.MapActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkAddEditActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkDetailsActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkListActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.SensorAddEditActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.SensorDetailsActivity;
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
    abstract NetworkDetailsActivity bindNetworkDetailActivity();

    @ContributesAndroidInjector
    abstract NetworkAddEditActivity bindNetworkAddEditActivity();

    @ContributesAndroidInjector
    abstract SensorListActivity bindSensorListActivity();

    @ContributesAndroidInjector
    abstract SensorDetailsActivity bindSensorDetailActivity();

    @ContributesAndroidInjector
    abstract SensorAddEditActivity bindSensorAddEditActivity();

    @ContributesAndroidInjector
    abstract ActuatorListActivity bindActuatorListActivity();

    @ContributesAndroidInjector
    abstract ActuatorDetailsActivity bindActuatorDetailActivity();

    @ContributesAndroidInjector
    abstract ActuatorAddEditActivity bindActuatorAddEditActivity();

    @ContributesAndroidInjector
    abstract MapActivity bindMapActivity();

    @ContributesAndroidInjector
    abstract SettingsActivity binSettingsActivity();

}