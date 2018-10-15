package com.m3sv.droidupnp.upnp.observers

import com.m3sv.droidupnp.data.UpnpDeviceEvent
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.droidupnp.model.upnp.ContentDirectoryDiscovery
import org.droidupnp.model.upnp.DeviceDiscoveryObserver


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