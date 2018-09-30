package com.m3sv.droidupnp.upnp.observers

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import org.droidupnp.model.upnp.DeviceDiscoveryObserver
import org.droidupnp.model.upnp.IUpnpDevice
import org.droidupnp.model.upnp.RendererDiscovery

class RendererDiscoveryObservable(private val rendererDiscovery: RendererDiscovery) :
    Observable<IUpnpDevice>() {
    override fun subscribeActual(observer: Observer<in IUpnpDevice>) {
        val deviceObserver = RendererDeviceObserver(rendererDiscovery, observer)
        observer.onSubscribe(deviceObserver)
    }

    private inner class RendererDeviceObserver(
        private val rendererDiscovery: RendererDiscovery,
        private val observer: Observer<in IUpnpDevice>
    ) :
        MainThreadDisposable(), DeviceDiscoveryObserver {

        init {
            rendererDiscovery.addObserver(this)
        }

        override fun onDispose() {
            rendererDiscovery.removeObserver(this)
        }

        override fun addedDevice(device: IUpnpDevice?) {
            if (!isDisposed && device != null)
                observer.onNext(device)
        }

        override fun removedDevice(device: IUpnpDevice?) {
        }
    }
}