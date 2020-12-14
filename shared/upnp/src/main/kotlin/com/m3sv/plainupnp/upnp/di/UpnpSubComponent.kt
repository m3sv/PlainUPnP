package com.m3sv.plainupnp.upnp.di

import com.m3sv.plainupnp.upnp.PlainUpnpAndroidService
import dagger.Subcomponent


interface UpnpSubComponentProvider {
    val upnpSubComponent: UpnpSubComponent
}

@Subcomponent
interface UpnpSubComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): UpnpSubComponent
    }

    fun inject(service: PlainUpnpAndroidService)

}
