package com.m3sv.droidupnp.upnp.observers

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import org.droidupnp.model.upnp.IDeviceDiscoveryObserver
import org.droidupnp.model.upnp.IUPnPDevice
import org.droidupnp.model.upnp.RendererDiscovery

class RendererDiscoveryObservable(private val rendererDiscovery: RendererDiscovery) : Observable<IUPnPDevice>() {
    override fun subscribeActual(observer: Observer<in IUPnPDevice>) {
        val deviceObserver = RendererDeviceObserver(rendererDiscovery, observer)
        observer.onSubscribe(deviceObserver)
    }

    private inner class RendererDeviceObserver(private val rendererDiscovery: RendererDiscovery,
                                               private val observer: Observer<in IUPnPDevice>) :
            MainThreadDisposable(), IDeviceDiscoveryObserver {

        init {
            rendererDiscovery.addObserver(this)
        }

        override fun onDispose() {
            rendererDiscovery.removeObserver(this)
        }

        override fun addedDevice(device: IUPnPDevice?) {
            if (!isDisposed && device != null)
                observer.onNext(device)
        }

        override fun removedDevice(device: IUPnPDevice?) {
        }
    }
}