package com.m3sv.plainupnp.upnp


import android.content.Context
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.DeviceType
import com.m3sv.plainupnp.data.upnp.LocalDevice
import com.m3sv.plainupnp.data.upnp.UpnpDeviceEvent
import com.m3sv.upnp.R
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import timber.log.Timber

class RendererDiscoveryObservable(
        private val controller: UpnpServiceController,
        private val playLocally: String
) : Observable<Set<DeviceDisplay>>() {

    private val renderers = LinkedHashSet<DeviceDisplay>().apply {
        add(DeviceDisplay(LocalDevice(playLocally)))
    }

    private fun handleEvent(event: UpnpDeviceEvent) {
        when (event) {
            is UpnpDeviceEvent.Added -> {
                Timber.d("Renderer added: ${event.upnpDevice.displayString}")
                renderers += DeviceDisplay(
                        event.upnpDevice,
                        false,
                        DeviceType.RENDERER
                )
            }

            is UpnpDeviceEvent.Removed -> {
                Timber.d("Renderer added: ${event.upnpDevice.displayString}")
                renderers -= DeviceDisplay(
                        event.upnpDevice,
                        false,
                        DeviceType.RENDERER
                )
            }
        }
    }

    override fun subscribeActual(observer: Observer<in Set<DeviceDisplay>>) {
        with(observer) {
            onSubscribe(RendererDeviceObserver(controller.rendererDiscovery, observer))
            onNext(renderers)
        }
    }

    private inner class RendererDeviceObserver(
            private val rendererDiscovery: RendererDiscovery,
            private val observer: Observer<in Set<DeviceDisplay>>
    ) : MainThreadDisposable(), DeviceDiscoveryObserver {

        init {
            rendererDiscovery.addObserver(this)
        }

        override fun onDispose() {
            rendererDiscovery.removeObserver(this)
        }

        override fun addedDevice(event: UpnpDeviceEvent) {
            handleEvent(event)

            if (!isDisposed) {
                observer.onNext(renderers)
            }
        }

        override fun removedDevice(event: UpnpDeviceEvent) {
            handleEvent(event)

            if (!isDisposed)
                observer.onNext(renderers)
        }
    }
}