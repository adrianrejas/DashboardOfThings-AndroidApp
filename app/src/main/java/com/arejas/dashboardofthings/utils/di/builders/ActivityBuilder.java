package com.arejas.dashboardofthings.utils.di.builders;

import com.arejas.dashboardofthings.presentation.ui.activities.MainDashboardActivity;
import com.arejas.dashboardofthings.presentation.ui.activities.MapActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuilder {

    @ContributesAndroidInjector
    abstract MainDashboardActivity bindMainDashboardActivity();

    @ContributesAndroidInjector
    abstract MapActivity bindMapActivity();

}