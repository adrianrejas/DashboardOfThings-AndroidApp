package com.arejas.dashboardofthings.utils.di.modules;


import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.data.sources.database.DotDatabase;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private static final int THREADS_FOR_HTTP_EXECUTOR = 5;
    private static final int THREADS_FOR_DB_DATAINSER = 5;

    @Provides
    Application provideApplication() {
        return DotApplication.application;
    }

    @Provides
    Context provideApplicationContext() {
        return DotApplication.application.getApplicationContext();
    }

    @Provides
    @Named("httpExecutor")
    Executor provideHttpExecutor() {
        return Executors.newFixedThreadPool(THREADS_FOR_HTTP_EXECUTOR);
    }

    @Provides
    @Named("dbExecutorManagement")
    Executor provideDbManagementExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Provides
    @Named("dbExecutorDataInsert")
    Executor provideDbDataInsertExecutor() {
        return Executors.newFixedThreadPool(THREADS_FOR_DB_DATAINSER);
    }

    @Provides
    @Singleton
    DotDatabase provideDatabase(DotApplication application) {
        return Room.databaseBuilder(application,
                DotDatabase.class, DotDatabase.DOT_DB_NAME)
                .build();
    }

}