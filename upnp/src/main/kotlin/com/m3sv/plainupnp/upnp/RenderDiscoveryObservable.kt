package com.m3sv.plainupnp.upnp


import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.DeviceType
import com.m3sv.plainupnp.data.upnp.LocalDevice
import com.m3sv.plainupnp.data.upnp.UpnpDeviceEvent
import com.m3sv.plainupnp.upnp.resourceproviders.UpnpResourceProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber
import javax.inject.Inject


class RendererDiscoveryObservable @Inject constructor(
    private val controller: UpnpServiceController,
    upnpResourceProvider: UpnpResourceProvider
) {
    private val renderers =
        LinkedHashSet<DeviceDisplay>(listOf(DeviceDisplay(LocalDevice(upnpResourceProvider.playLocally))))

    val currentRenderers: List<DeviceDisplay>
        get() = renderers.toList()

    @ExperimentalCoroutinesApi
    fun observe() = callbackFlow<List<DeviceDisplay>> {
        val callback = object : DeviceDiscoveryObserver {
            override fun addedDevice(event: UpnpDeviceEvent) {
                handleEvent(event)
                sendRenderers()
            }

            override fun removedDevice(event: UpnpDeviceEvent) {
                handleEvent(event)
                sendRenderers()
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
                        Timber.d("Renderer removed: ${event.upnpDevice.displayString}")
                        renderers -= DeviceDisplay(
                            event.upnpDevice,
                            false,
                            DeviceType.RENDERER
                        )
                    }
                }
            }

            private fun sendRenderers() {
                if (!isClosedForSend)
                    sendBlocking(currentRenderers)
            }
        }

        controller.rendererDiscovery.addObserver(callback)
        sendBlocking(currentRenderers)
        awaitClose { controller.rendererDiscovery.removeObserver(callback) }
    }
}
