package com.arejas.dashboardofthings.utils.di.modules;


import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import com.arejas.dashboardofthings.DotApplication;
import com.arejas.dashboardofthings.data.interfaces.DotRepository;
import com.arejas.dashboardofthings.data.sources.database.DotDatabase;
import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.NetworkManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase;
import com.arejas.dashboardofthings.domain.usecases.implementations.ActuatorManagementUseCaseImpl;
import com.arejas.dashboardofthings.domain.usecases.implementations.DataManagementUseCaseImpl;
import com.arejas.dashboardofthings.domain.usecases.implementations.LogsManagementUseCaseImpl;
import com.arejas.dashboardofthings.domain.usecases.implementations.NetworkManagementUseCaseImpl;
import com.arejas.dashboardofthings.domain.usecases.implementations.SensorManagementUseCaseImpl;

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

    @Provides
    @Singleton
    public NetworkManagementUseCase provideNetworkManagementUseCase (DotRepository repository) {
        return new NetworkManagementUseCaseImpl(repository);
    }

    @Provides
    @Singleton
    public SensorManagementUseCase provideSensorManagementUseCase (DotRepository repository) {
        return new SensorManagementUseCaseImpl(repository);
    }

    @Provides
    @Singleton
    public ActuatorManagementUseCase provideActuatorManagementUseCase (DotRepository repository) {
        return new ActuatorManagementUseCaseImpl(repository);
    }

    @Provides
    @Singleton
    public DataManagementUseCase provideDataRequestUseCase (DotRepository repository) {
        return new DataManagementUseCaseImpl(repository);
    }

    @Provides
    @Singleton
    public LogsManagementUseCase provideLogsRequestUseCase (DotRepository repository) {
        return new LogsManagementUseCaseImpl(repository);
    }

}