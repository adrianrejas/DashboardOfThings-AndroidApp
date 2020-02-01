package com.arejas.dashboardofthings.utils.di.components

import com.arejas.dashboardofthings.DotApplication
import com.arejas.dashboardofthings.utils.di.builders.ActivityBuilder
import com.arejas.dashboardofthings.utils.di.builders.FragmentBuilder
import com.arejas.dashboardofthings.utils.di.builders.ServiceBuilder
import com.arejas.dashboardofthings.utils.di.modules.AppModule

import javax.inject.Singleton

import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector

@Singleton
@Component(modules = [AndroidInjectionModule::class, AppModule::class, ActivityBuilder::class, FragmentBuilder::class, ServiceBuilder::class])
interface AppComponent : AndroidInjector<DotApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: DotApplication): Builder

        fun appModule(module: AppModule): Builder

        fun build(): AppComponent

    }

    override fun inject(app: DotApplication)

}
