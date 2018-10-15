package org.droidupnp.model.upnp

import com.m3sv.droidupnp.data.UpnpDevice

import java.util.concurrent.Callable

interface ICallableFilter : Callable<Boolean> {
    fun setDevice(device: UpnpDevice)
}
