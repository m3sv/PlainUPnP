package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.upnp.store.UpnpDirectory
import dagger.Binds
import dagger.Module


@Module
interface HomeFragmentModule {

    @Binds
    fun bindHomeDirectoryBinder(upnpDirectoryMapper: UpnpDirectoryMapper): (UpnpDirectory) -> UpnpFolder
}
