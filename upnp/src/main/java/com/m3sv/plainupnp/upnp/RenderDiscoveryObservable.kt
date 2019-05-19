package com.m3sv.plainupnp.upnp


import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.DeviceType
import com.m3sv.plainupnp.data.upnp.LocalDevice
import com.m3sv.plainupnp.data.upnp.UpnpDeviceEvent
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import timber.log.Timber
import javax.inject.Inject

class RendererDiscoveryObservable @Inject constructor(
        private val controller: UpnpServiceController
) : Observable<Set<DeviceDisplay>>() {

    private val renderers = LinkedHashSet<DeviceDisplay>().apply {
        // TODO use resource provider
        add(DeviceDisplay(LocalDevice("play locally")))
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