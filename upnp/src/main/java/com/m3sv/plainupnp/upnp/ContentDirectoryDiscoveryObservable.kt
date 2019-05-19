package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.DeviceType
import com.m3sv.plainupnp.data.upnp.UpnpDeviceEvent
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import timber.log.Timber
import javax.inject.Inject


class ContentDirectoryDiscoveryObservable @Inject constructor(private val controller: UpnpServiceController) :
        Observable<Set<DeviceDisplay>>() {
    private val contentDirectories = LinkedHashSet<DeviceDisplay>()

    override fun subscribeActual(observer: Observer<in Set<DeviceDisplay>>) {
        val deviceObserver = ContentDeviceObserver(controller.contentDirectoryDiscovery, observer)
        observer.onSubscribe(deviceObserver)
    }

    private fun handleEvent(event: UpnpDeviceEvent) {
        when (event) {
            is UpnpDeviceEvent.Added -> {
                Timber.d("Content directory added: ${event.upnpDevice.friendlyName}")

                contentDirectories += DeviceDisplay(
                        event.upnpDevice,
                        false,
                        DeviceType.CONTENT_DIRECTORY
                )
            }

            is UpnpDeviceEvent.Removed -> {
                Timber.d("Content directory removed: ${event.upnpDevice.friendlyName}")

                contentDirectories -= DeviceDisplay(
                        event.upnpDevice,
                        false,
                        DeviceType.CONTENT_DIRECTORY
                )
            }
        }

    }

    private inner class ContentDeviceObserver(
            private val contentDirectoryDiscovery: DeviceDiscovery,
            private val observer: Observer<in Set<DeviceDisplay>>
    ) : Disposable, DeviceDiscoveryObserver {

        init {
            contentDirectoryDiscovery.addObserver(this)
        }

        override fun isDisposed(): Boolean = !contentDirectoryDiscovery.hasObserver(this)

        override fun dispose() {
            contentDirectoryDiscovery.removeObserver(this)
        }

        override fun addedDevice(event: UpnpDeviceEvent) {
            handleEvent(event)
            if (!isDisposed)
                observer.onNext(contentDirectories)
        }

        override fun removedDevice(event: UpnpDeviceEvent) {
            handleEvent(event)
            if (!isDisposed)
                observer.onNext(contentDirectories)
        }
    }
}