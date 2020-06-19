package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.DeviceType
import com.m3sv.plainupnp.data.upnp.UpnpDeviceEvent
import com.m3sv.plainupnp.upnp.discovery.device.DeviceDiscoveryObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject


class ContentDirectoryDiscoveryObservable @Inject constructor(private val controller: UpnpServiceController) {

    private val contentDirectories = LinkedHashSet<DeviceDisplay>()

    val currentContentDirectories: List<DeviceDisplay>
        get() = contentDirectories.toList()

    @ExperimentalCoroutinesApi
    fun subscribe() = callbackFlow<List<DeviceDisplay>> {
        val callback = object :
            DeviceDiscoveryObserver {
            override fun addedDevice(event: UpnpDeviceEvent) {
                handleEvent(event)
                sendContentDirectories()
            }

            override fun removedDevice(event: UpnpDeviceEvent) {
                handleEvent(event)
                sendContentDirectories()
            }

            private fun handleEvent(event: UpnpDeviceEvent) {
                when (event) {
                    is UpnpDeviceEvent.Added -> {
                        contentDirectories += DeviceDisplay(
                            event.upnpDevice,
                            false,
                            DeviceType.CONTENT_DIRECTORY
                        )
                    }

                    is UpnpDeviceEvent.Removed -> {
                        val device = DeviceDisplay(
                            event.upnpDevice,
                            false,
                            DeviceType.CONTENT_DIRECTORY
                        )

                        if (contentDirectories.contains(device))
                            contentDirectories -= device
                    }
                }
            }

            private fun sendContentDirectories() {
                if (!isClosedForSend)
                    sendBlocking(currentContentDirectories)
            }
        }

        controller.contentDirectoryDiscovery.addObserver(callback)

        awaitClose { controller.contentDirectoryDiscovery.removeObserver(callback) }
    }
}
