package com.arejas.dashboardofthings;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;

import com.arejas.dashboardofthings.utils.di.components.DaggerAppComponent;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;

public class DotApplication extends Application implements HasActivityInjector, HasServiceInjector {

    public static Application application;

    public static Application getApplication() {
        return application;
    }

    @Inject
    public DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    @Inject
    public DispatchingAndroidInjector<Service> dispatchingServiceInjector;

    public static Context getContext() {
        return application.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        application = this;
        this.initDagger();
    }

    protected void initDagger(){
        DaggerAppComponent.builder()
                .application(this)
                .build()
                .inject(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }
}
