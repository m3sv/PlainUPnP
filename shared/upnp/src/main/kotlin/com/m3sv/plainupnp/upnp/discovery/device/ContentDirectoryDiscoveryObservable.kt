package com.m3sv.plainupnp.upnp.discovery.device

import com.m3sv.plainupnp.common.BackgroundModeManager
import com.m3sv.plainupnp.common.util.doNothing
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.DeviceType
import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.data.upnp.UpnpDeviceEvent
import com.m3sv.plainupnp.upnp.CDevice
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.callbackFlow
import org.fourthline.cling.model.meta.LocalDevice
import timber.log.Timber
import javax.inject.Inject


class ContentDirectoryDiscoveryObservable @Inject constructor(
    private val contentDirectoryDiscovery: ContentDirectoryDiscovery,
    private val backgroundModeManager: BackgroundModeManager,
) {
    var selectedContentDirectory: UpnpDevice? = null

    private val contentDirectories = LinkedHashSet<DeviceDisplay>()

    val currentContentDirectories: List<DeviceDisplay>
        get() = contentDirectories.toList()

    operator fun invoke() = callbackFlow<List<DeviceDisplay>> {
        contentDirectoryDiscovery.startObserving()

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
                        Timber.d("Content directory added: ${event.upnpDevice.displayString}")
                        contentDirectories += DeviceDisplay(
                            event.upnpDevice,
                            false,
                            DeviceType.CONTENT_DIRECTORY
                        )
                    }

                    is UpnpDeviceEvent.Removed -> {
                        Timber.d("Content directory removed: ${event.upnpDevice.displayString}")

                        val device = DeviceDisplay(
                            event.upnpDevice,
                            false,
                            DeviceType.CONTENT_DIRECTORY
                        )

                        if (contentDirectories.contains(device))
                            contentDirectories -= device

                        when {
                            !backgroundModeManager.isAllowedToRunInBackground()
                                    && (event.upnpDevice as CDevice).device is LocalDevice
                                    && event.upnpDevice == selectedContentDirectory
                            -> doNothing

                            event.upnpDevice == selectedContentDirectory ->
                                selectedContentDirectory = null
                        }
                    }
                }
            }

            private fun sendContentDirectories() {
                if (!isClosedForSend)
                    sendBlocking(currentContentDirectories)
            }
        }

        contentDirectoryDiscovery.addObserver(callback)

        awaitClose { contentDirectoryDiscovery.removeObserver(callback) }
    }
}
