package com.h.raspberrysensor.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.h.raspberrysensor.features.discovery.DiscoveryViewModel
import com.h.raspberrysensor.features.main.MainViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(DiscoveryViewModel::class)
    abstract fun bindDiscoveryViewModel(discoveryViewModel: DiscoveryViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}
