package com.arejas.dashboardofthings.utils.di.builders


import com.arejas.dashboardofthings.domain.services.ControlService
import com.arejas.dashboardofthings.presentation.ui.widget.SensorWidgetService

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilder {

    @ContributesAndroidInjector
    internal abstract fun bindControlService(): ControlService

    @ContributesAndroidInjector
    internal abstract fun bindSensorWidgetService(): SensorWidgetService

}
