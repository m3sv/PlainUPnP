package com.m3sv.plainupnp.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.m3sv.plainupnp.common.Filter
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.presentation.home.HomeViewModel
import com.m3sv.plainupnp.presentation.main.MainViewModel
import com.m3sv.plainupnp.upnp.android.AndroidUpnpServiceImpl
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import org.fourthline.cling.UpnpService
import javax.inject.Singleton

@Module
abstract class BinderModule {

    @Binds
    @Singleton
    abstract fun bindFilterDelegate(filter: Filter): FilterDelegate

    @Binds
    @Singleton
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    abstract fun bindUpnpService(service: AndroidUpnpServiceImpl): UpnpService

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    abstract fun bindHomeFragmentViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainActivityViewModel(mainViewModel: MainViewModel): ViewModel
}
