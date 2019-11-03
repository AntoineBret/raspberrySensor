package com.h.raspberrysensor.di

import android.content.Context
import android.content.SharedPreferences
import com.h.raspberrysensor.RaspberrySensorApp
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

private const val PREFERENCE_KEY = "PREFERENCE_KEY"

@Module
object AppModule {

    @Provides
    @JvmStatic
    @Singleton
    fun provideSharedPreferences(application: RaspberrySensorApp): SharedPreferences {
        return application.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)
    }

}
