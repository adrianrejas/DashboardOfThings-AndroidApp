package com.arejas.dashboardofthings.utils.di.builders;


import com.arejas.dashboardofthings.domain.services.ControlService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ServiceBuilder {

    @ContributesAndroidInjector
    abstract ControlService bindControlService();

}
