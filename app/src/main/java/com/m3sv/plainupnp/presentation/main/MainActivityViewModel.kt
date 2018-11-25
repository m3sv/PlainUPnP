package com.m3sv.plainupnp.presentation.main

import android.arch.lifecycle.MutableLiveData
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.DeviceType
import com.m3sv.plainupnp.data.upnp.Directory
import com.m3sv.plainupnp.data.upnp.UpnpDeviceEvent
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.upnp.UpnpManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val defaultUpnpManager: UpnpManager
) :
    BaseViewModel(), UpnpManager by defaultUpnpManager {

    var currentDirectory: Directory? = null

    val contentDirectoriesObservable = MutableLiveData<Set<DeviceDisplay>>()

    val renderersObservable = MutableLiveData<Set<DeviceDisplay>>()

    private val discoveryDisposable: CompositeDisposable = CompositeDisposable()

    private val renderers = LinkedHashSet<DeviceDisplay>()

    private val contentDirectories = LinkedHashSet<DeviceDisplay>()

    private val errorHandler: (Throwable) -> Unit =
        { Timber.e("Exception during discovery: ${it.message}") }

    init {
        disposables += selectedDirectoryObservable
            .subscribeBy(
                onNext = {
                    currentDirectory = it
                }, onError = Timber::e
            )

        discoveryDisposable += rendererDiscoveryObservable
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onNext = { event ->
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

                    renderersObservable.postValue(renderers)
                },
                onError = errorHandler
            )

        discoveryDisposable += contentDirectoryDiscoveryObservable
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onNext = { event ->
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

                            contentDirectories -= DeviceDisplay(
                                event.upnpDevice,
                                false,
                                DeviceType.CONTENT_DIRECTORY
                            )
                        }
                    }

                    contentDirectoriesObservable.postValue(contentDirectories)
                }, onError = errorHandler
            )
    }

    fun resumeUpnp() {
        Timber.d("Resuming UPnP controller")
        resumeRendererUpdate()
    }

    fun pauseUpnp() {
        Timber.d("Pausing UPnP controller")
        pauseRendererUpdate()
    }
}
