package com.m3sv.plainupnp.upnp.observables

import com.m3sv.plainupnp.data.upnp.UpnpDeviceEvent
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import com.m3sv.plainupnp.upnp.discovery.ContentDirectoryDiscovery
import org.droidupnp.legacy.upnp.DeviceDiscoveryObserver


class ContentDirectoryDiscoveryObservable(private val contentDiscovery: ContentDirectoryDiscovery) :
    Observable<UpnpDeviceEvent>() {
    override fun subscribeActual(observer: Observer<in UpnpDeviceEvent>) {
        val deviceObserver = ContentDeviceObserver(contentDiscovery, observer)
        observer.onSubscribe(deviceObserver)
    }

    private inner class ContentDeviceObserver(
        private val contentDirectoryDiscovery: ContentDirectoryDiscovery,
        private val observer: Observer<in UpnpDeviceEvent>
    ) :
        Disposable, DeviceDiscoveryObserver {
        init {
            contentDirectoryDiscovery.addObserver(this)
        }

        override fun isDisposed(): Boolean = !contentDirectoryDiscovery.hasObserver(this)

        override fun dispose() {
            contentDirectoryDiscovery.removeObserver(this)
        }

        override fun addedDevice(device: UpnpDeviceEvent) {
            if (!isDisposed)
                observer.onNext(device)
        }

        override fun removedDevice(device: UpnpDeviceEvent) {
            if (!isDisposed)
                observer.onNext(device)
        }
    }
}