package com.h.raspberrysensor.di

import com.h.raspberrysensor.RaspberrySensorApp
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, ActivityModule::class, AppModule::class, ViewModelModule::class])
interface AppComponent {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: RaspberrySensorApp): Builder

        fun build(): AppComponent
    }

    fun inject(raspberrySensorApp: RaspberrySensorApp)
}
