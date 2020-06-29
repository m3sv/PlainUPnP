package com.m3sv.plainupnp.presentation.home

import androidx.lifecycle.ViewModel
import com.m3sv.plainupnp.common.Mapper
import com.m3sv.plainupnp.di.ViewModelKey
import com.m3sv.plainupnp.upnp.store.UpnpDirectory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap


@Module
interface HomeFragmentModule {

    @Binds
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun bindHomeFragmentViewModel(homeViewModel: HomeViewModel): ViewModel

    @Binds
    fun bindHomeDirectoryBinder(homeUpnpDirectoryMapper: HomeUpnpDirectoryMapper): Mapper<UpnpDirectory, Directory>
}
