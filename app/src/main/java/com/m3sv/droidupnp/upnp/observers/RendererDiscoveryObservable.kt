package com.m3sv.droidupnp.upnp.observers

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import org.droidupnp.model.upnp.DeviceDiscoveryObserver
import com.m3sv.droidupnp.data.UpnpDevice
import com.m3sv.droidupnp.data.UpnpDeviceEvent
import org.droidupnp.model.upnp.RendererDiscovery

class RendererDiscoveryObservable(private val rendererDiscovery: RendererDiscovery) :
    Observable<UpnpDeviceEvent>() {
    override fun subscribeActual(observer: Observer<in UpnpDeviceEvent>) {
        val deviceObserver = RendererDeviceObserver(rendererDiscovery, observer)
        observer.onSubscribe(deviceObserver)
    }

    private inner class RendererDeviceObserver(
        private val rendererDiscovery: RendererDiscovery,
        private val observer: Observer<in UpnpDeviceEvent>
    ) :
        MainThreadDisposable(), DeviceDiscoveryObserver {

        init {
            rendererDiscovery.addObserver(this)
        }

        override fun onDispose() {
            rendererDiscovery.removeObserver(this)
        }

        override fun addedDevice(device: UpnpDeviceEvent?) {
            if (!isDisposed && device != null)
                observer.onNext(device)
        }

        override fun removedDevice(device: UpnpDeviceEvent?) {
            if (!isDisposed && device != null)
                observer.onNext(device)
        }
    }
}