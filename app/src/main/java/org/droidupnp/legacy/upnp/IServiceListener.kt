package org.droidupnp.legacy.upnp

import android.content.ServiceConnection

import com.m3sv.plainupnp.data.upnp.UpnpDevice

interface IServiceListener {

    val deviceList: Collection<UpnpDevice>

    val serviceConnection: ServiceConnection

    fun addListener(registryListener: IRegistryListener)

    fun removeListener(registryListener: IRegistryListener)

    fun clearListener()

    fun refresh()

    fun getFilteredDeviceList(filter: ICallableFilter): Collection<UpnpDevice>
}
