package com.arejas.dashboardofthings.utils.di.modules


import android.app.Application
import android.content.Context

import androidx.room.Room

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.data.interfaces.DotRepository
import com.arejas.dashboardofthings.data.sources.database.DotDatabase
import com.arejas.dashboardofthings.domain.usecases.ActuatorManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.DataManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.LogsManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.NetworkManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.SensorManagementUseCase
import com.arejas.dashboardofthings.domain.usecases.implementations.ActuatorManagementUseCaseImpl
import com.arejas.dashboardofthings.domain.usecases.implementations.DataManagementUseCaseImpl
import com.arejas.dashboardofthings.domain.usecases.implementations.LogsManagementUseCaseImpl
import com.arejas.dashboardofthings.domain.usecases.implementations.NetworkManagementUseCaseImpl
import com.arejas.dashboardofthings.domain.usecases.implementations.SensorManagementUseCaseImpl

import java.util.concurrent.Executor
import java.util.concurrent.Executors

import javax.inject.Named
import javax.inject.Singleton

import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    internal fun provideApplication(): Application {
        return DotApplication.application
    }

    @Provides
    internal fun provideApplicationContext(): Context {
        return DotApplication.application.applicationContext
    }

    @Provides
    @Named("httpExecutor")
    internal fun provideHttpExecutor(): Executor {
        return Executors.newFixedThreadPool(THREADS_FOR_HTTP_EXECUTOR)
    }

    @Provides
    @Named("dbExecutorManagement")
    internal fun provideDbManagementExecutor(): Executor {
        return Executors.newSingleThreadExecutor()
    }

    @Provides
    @Named("dbExecutorDataInsert")
    internal fun provideDbDataInsertExecutor(): Executor {
        return Executors.newFixedThreadPool(THREADS_FOR_DB_DATAINSER)
    }

    @Provides
    @Singleton
    internal fun provideDatabase(application: DotApplication): DotDatabase {
        return Room.databaseBuilder(
            application,
            DotDatabase::class.java, DotDatabase.DOT_DB_NAME
        )
            .build()
    }

    @Provides
    @Singleton
    fun provideNetworkManagementUseCase(repository: DotRepository): NetworkManagementUseCase {
        return NetworkManagementUseCaseImpl(repository)
    }

    @Provides
    @Singleton
    fun provideSensorManagementUseCase(repository: DotRepository): SensorManagementUseCase {
        return SensorManagementUseCaseImpl(repository)
    }

    @Provides
    @Singleton
    fun provideActuatorManagementUseCase(repository: DotRepository): ActuatorManagementUseCase {
        return ActuatorManagementUseCaseImpl(repository)
    }

    @Provides
    @Singleton
    fun provideDataRequestUseCase(repository: DotRepository): DataManagementUseCase {
        return DataManagementUseCaseImpl(repository)
    }

    @Provides
    @Singleton
    fun provideLogsRequestUseCase(repository: DotRepository): LogsManagementUseCase {
        return LogsManagementUseCaseImpl(repository)
    }

    companion object {

        private val THREADS_FOR_HTTP_EXECUTOR = 5
        private val THREADS_FOR_DB_DATAINSER = 5
    }

}