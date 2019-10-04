package com.arejas.dashboardofthings.utils.di.modules;


import android.app.Application;
import android.content.Context;

import com.arejas.dashboardofthings.DotApplication;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private static final int THREADS_FOR_GENERAL_EXECUTOR = 3;

    @Provides
    Application provideApplication() {
        return DotApplication.application;
    }

    @Provides
    Context provideApplicationContext() {
        return DotApplication.application.getApplicationContext();
    }

}