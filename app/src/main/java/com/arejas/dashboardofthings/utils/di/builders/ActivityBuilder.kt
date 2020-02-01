package com.arejas.dashboardofthings.utils.di.builders

import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorAddEditActivity
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorDetailsActivity
import com.arejas.dashboardofthings.presentation.ui.activities.ActuatorListActivity
import com.arejas.dashboardofthings.presentation.ui.activities.MainDashboardActivity
import com.arejas.dashboardofthings.presentation.ui.activities.MapActivity
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkAddEditActivity
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkDetailsActivity
import com.arejas.dashboardofthings.presentation.ui.activities.NetworkListActivity
import com.arejas.dashboardofthings.presentation.ui.activities.SensorAddEditActivity
import com.arejas.dashboardofthings.presentation.ui.activities.SensorDetailsActivity
import com.arejas.dashboardofthings.presentation.ui.activities.SensorListActivity
import com.arejas.dashboardofthings.presentation.ui.activities.SettingsActivity
import com.arejas.dashboardofthings.presentation.ui.widget.SelectSensorForWidgetActivity

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector
    internal abstract fun bindMainDashboardActivity(): MainDashboardActivity

    @ContributesAndroidInjector
    internal abstract fun bindNetworkListActivity(): NetworkListActivity

    @ContributesAndroidInjector
    internal abstract fun bindNetworkDetailActivity(): NetworkDetailsActivity

    @ContributesAndroidInjector
    internal abstract fun bindNetworkAddEditActivity(): NetworkAddEditActivity

    @ContributesAndroidInjector
    internal abstract fun bindSensorListActivity(): SensorListActivity

    @ContributesAndroidInjector
    internal abstract fun bindSensorDetailActivity(): SensorDetailsActivity

    @ContributesAndroidInjector
    internal abstract fun bindSensorAddEditActivity(): SensorAddEditActivity

    @ContributesAndroidInjector
    internal abstract fun bindActuatorListActivity(): ActuatorListActivity

    @ContributesAndroidInjector
    internal abstract fun bindActuatorDetailActivity(): ActuatorDetailsActivity

    @ContributesAndroidInjector
    internal abstract fun bindActuatorAddEditActivity(): ActuatorAddEditActivity

    @ContributesAndroidInjector
    internal abstract fun bindMapActivity(): MapActivity

    @ContributesAndroidInjector
    internal abstract fun binSettingsActivity(): SettingsActivity

    @ContributesAndroidInjector
    internal abstract fun binSelectSensorForWidgetActivity(): SelectSensorForWidgetActivity

}