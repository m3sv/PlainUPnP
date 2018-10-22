package org.droidupnp.legacy.upnp

import com.m3sv.plainupnp.data.UpnpDevice

import java.util.concurrent.Callable

interface ICallableFilter : Callable<Boolean> {
    fun setDevice(device: UpnpDevice)
}
