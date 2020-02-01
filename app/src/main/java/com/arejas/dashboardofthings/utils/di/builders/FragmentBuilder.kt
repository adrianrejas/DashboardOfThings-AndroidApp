package com.arejas.dashboardofthings.utils.di.builders

import com.arejas.dashboardofthings.presentation.ui.fragments.ActuatorDetailsDetailsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.ActuatorDetailsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.ActuatorDetailsLogsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.MainActuatorsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.MainHistoryFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.MainLogsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.MainSensorsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.MainStatusFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailsDetailsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.NetworkDetailsLogsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsDetailsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsFragment
import com.arejas.dashboardofthings.presentation.ui.fragments.SensorDetailsLogsFragment

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilder {

    @ContributesAndroidInjector
    internal abstract fun bindMainActuatorsFragment(): MainActuatorsFragment

    @ContributesAndroidInjector
    internal abstract fun bindMainHistoryFragment(): MainHistoryFragment

    @ContributesAndroidInjector
    internal abstract fun bindMainLogsFragment(): MainLogsFragment

    @ContributesAndroidInjector
    internal abstract fun bindMainSensorsFragment(): MainSensorsFragment

    @ContributesAndroidInjector
    internal abstract fun bindMainStatusFragment(): MainStatusFragment

    @ContributesAndroidInjector
    internal abstract fun bindNetworkDetailFragment(): NetworkDetailsFragment

    @ContributesAndroidInjector
    internal abstract fun bindNetworkDetailDetailsFragment(): NetworkDetailsDetailsFragment

    @ContributesAndroidInjector
    internal abstract fun bindNetworkDetailLogsFragment(): NetworkDetailsLogsFragment

    @ContributesAndroidInjector
    internal abstract fun bindSensorDetailFragment(): SensorDetailsFragment

    @ContributesAndroidInjector
    internal abstract fun bindSensorDetailDetailsFragment(): SensorDetailsDetailsFragment

    @ContributesAndroidInjector
    internal abstract fun bindSensorDetailLogsFragment(): SensorDetailsLogsFragment

    @ContributesAndroidInjector
    internal abstract fun bindActuatorDetailFragment(): ActuatorDetailsFragment

    @ContributesAndroidInjector
    internal abstract fun bindActuatorDetailDetailsFragment(): ActuatorDetailsDetailsFragment

    @ContributesAndroidInjector
    internal abstract fun bindActuatorDetailLogsFragment(): ActuatorDetailsLogsFragment

}