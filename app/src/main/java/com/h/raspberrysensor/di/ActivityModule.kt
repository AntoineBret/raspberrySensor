package com.h.raspberrysensor.di

import com.h.raspberrysensor.features.discovery.DiscoveryActivity
import com.h.raspberrysensor.features.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeDiscoveryActivity(): DiscoveryActivity
}
