package com.m3sv.plainupnp.upnp.observables

import android.content.Context
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.DeviceType
import com.m3sv.plainupnp.data.upnp.LocalDevice
import com.m3sv.plainupnp.data.upnp.UpnpDeviceEvent
import com.m3sv.plainupnp.upnp.discovery.RendererDiscovery
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import org.droidupnp.legacy.upnp.DeviceDiscoveryObserver
import timber.log.Timber

class RendererDiscoveryObservable(
    private val context: Context,
    private val rendererDiscovery: RendererDiscovery
) :
    Observable<Set<DeviceDisplay>>() {

    private val renderers = LinkedHashSet<DeviceDisplay>().apply {
        add(DeviceDisplay(LocalDevice(context.getString(R.string.play_locally))))
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
        val deviceObserver = RendererDeviceObserver(rendererDiscovery, observer)
        observer.onSubscribe(deviceObserver)
        observer.onNext(renderers)
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