package com.arejas.dashboardofthings

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Context
import android.view.View

import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment

import com.arejas.dashboardofthings.utils.di.components.DaggerAppComponent

import javax.inject.Inject

import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasFragmentInjector
import dagger.android.HasServiceInjector
import dagger.android.support.HasSupportFragmentInjector

class DotApplication : Application(), HasActivityInjector, HasSupportFragmentInjector,
    HasServiceInjector {

    @Inject
    var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>? = null

    @Inject
    var dispatchingServiceInjector: DispatchingAndroidInjector<Service>? = null

    @Inject
    var dispatchingFragmentInjector: DispatchingAndroidInjector<Fragment>? = null

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        application = this
        this.initDagger()
    }

    protected fun initDagger() {
        DaggerAppComponent.builder()
            .application(this)
            .build()
            .inject(this)
    }

    override fun activityInjector(): AndroidInjector<Activity>? {
        return dispatchingActivityInjector
    }

    override fun serviceInjector(): AndroidInjector<Service>? {
        return dispatchingServiceInjector
    }

    override fun supportFragmentInjector(): AndroidInjector<androidx.fragment.app.Fragment>? {
        return dispatchingFragmentInjector
    }

    companion object {

        lateinit var application: Application

        val context: Context
            get() = application.applicationContext
    }
}
