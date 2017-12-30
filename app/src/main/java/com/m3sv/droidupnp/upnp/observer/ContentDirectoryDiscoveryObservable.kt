package com.m3sv.droidupnp.upnp.observer

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import org.droidupnp.model.upnp.ContentDirectoryDiscovery
import org.droidupnp.model.upnp.IDeviceDiscoveryObserver
import org.droidupnp.model.upnp.IUPnPDevice


class ContentDirectoryDiscoveryObservable(private val contentDiscovery: ContentDirectoryDiscovery) : Observable<IUPnPDevice>() {
    override fun subscribeActual(observer: Observer<in IUPnPDevice>) {
        val deviceObserver = ContentDeviceObserver(contentDiscovery, observer)
        observer.onSubscribe(deviceObserver)
    }

    private inner class ContentDeviceObserver(private val contentDirectoryDiscovery: ContentDirectoryDiscovery,
                                              private val observer: Observer<in IUPnPDevice>) : MainThreadDisposable(), IDeviceDiscoveryObserver {
        init {
            contentDirectoryDiscovery.addObserver(this)
        }

        override fun onDispose() {
            contentDirectoryDiscovery.removeObserver(this)
        }

        override fun addedDevice(device: IUPnPDevice) {
            if (!isDisposed)
                observer.onNext(device)
        }

        override fun removedDevice(device: IUPnPDevice?) {
        }
    }
}