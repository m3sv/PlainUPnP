package com.m3sv.droidupnp.upnp.observers

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.droidupnp.model.upnp.ContentDirectoryDiscovery
import org.droidupnp.model.upnp.DeviceDiscoveryObserver
import org.droidupnp.model.upnp.IUpnpDevice


class ContentDirectoryDiscoveryObservable(private val contentDiscovery: ContentDirectoryDiscovery) :
    Observable<IUpnpDevice>() {
    override fun subscribeActual(observer: Observer<in IUpnpDevice>) {
        val deviceObserver = ContentDeviceObserver(contentDiscovery, observer)
        observer.onSubscribe(deviceObserver)
    }

    private inner class ContentDeviceObserver(
        private val contentDirectoryDiscovery: ContentDirectoryDiscovery,
        private val observer: Observer<in IUpnpDevice>
    ) :
        Disposable, DeviceDiscoveryObserver {
        init {
            contentDirectoryDiscovery.addObserver(this)
        }

        override fun isDisposed(): Boolean = !contentDirectoryDiscovery.hasObserver(this)

        override fun dispose() {
            contentDirectoryDiscovery.removeObserver(this)
        }

        override fun addedDevice(device: IUpnpDevice?) {
            if (!isDisposed && device != null)
                observer.onNext(device)
        }

        override fun removedDevice(device: IUpnpDevice?) {
        }
    }
}