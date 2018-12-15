package org.droidupnp.legacy.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice

import java.util.concurrent.Callable

interface CallableFilter : Callable<Boolean> {
    var device: UpnpDevice?
}
