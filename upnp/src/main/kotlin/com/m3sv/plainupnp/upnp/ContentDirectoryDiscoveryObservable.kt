package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.DeviceType
import com.m3sv.plainupnp.data.upnp.UpnpDeviceEvent
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
        val callback = object : DeviceDiscoveryObserver {
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

//class ContentDirectoryDiscoveryObservableRx @Inject constructor(private val controller: UpnpServiceController) :
//    Observable<List<DeviceDisplay>>() {
//
//    private val contentDirectories = LinkedHashSet<DeviceDisplay>()
//
//    fun currentContentDirectories() = contentDirectories.toList()
//
//    override fun subscribeActual(observer: Observer<in List<DeviceDisplay>>) {
//        val deviceObserver = ContentDeviceObserver(controller.contentDirectoryDiscovery, observer)
//        observer.onSubscribe(deviceObserver)
//    }
//
//    private inner class ContentDeviceObserver(
//        private val contentDirectoryDiscovery: DeviceDiscovery,
//        private val observer: Observer<in List<DeviceDisplay>>
//    ) : Disposable, DeviceDiscoveryObserver {
//
//        init {
//            contentDirectoryDiscovery.addObserver(this)
//        }
//
//        override fun isDisposed(): Boolean = !contentDirectoryDiscovery.hasObserver(this)
//
//        override fun dispose() {
//            contentDirectoryDiscovery.removeObserver(this)
//        }
//
//        override fun addedDevice(event: UpnpDeviceEvent) {
//            handleEvent(event)
//            if (!isDisposed)
//                observer.onNext(contentDirectories.toList())
//        }
//
//        override fun removedDevice(event: UpnpDeviceEvent) {
//            handleEvent(event)
//
//            if (!isDisposed)
//                observer.onNext(contentDirectories.toList())
//        }
//
//        private fun handleEvent(event: UpnpDeviceEvent) {
//            when (event) {
//                is UpnpDeviceEvent.Added -> {
//                    contentDirectories += DeviceDisplay(
//                        event.upnpDevice,
//                        false,
//                        DeviceType.CONTENT_DIRECTORY
//                    )
//                }
//
//                is UpnpDeviceEvent.Removed -> {
//                    val device = DeviceDisplay(
//                        event.upnpDevice,
//                        false,
//                        DeviceType.CONTENT_DIRECTORY
//                    )
//
//                    if (contentDirectories.contains(device))
//                        contentDirectories -= device
//                }
//            }
//
//        }
//    }
//}
