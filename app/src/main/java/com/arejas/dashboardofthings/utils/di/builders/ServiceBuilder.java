package com.arejas.dashboardofthings.utils.di.builders;


import com.arejas.dashboardofthings.domain.services.ControlService;
import com.arejas.dashboardofthings.presentation.ui.widget.SensorWidgetService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ServiceBuilder {

    @ContributesAndroidInjector
    abstract ControlService bindControlService();

    @ContributesAndroidInjector
    abstract SensorWidgetService bindSensorWidgetService();

}
