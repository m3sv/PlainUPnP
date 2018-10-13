package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.MutableLiveData
import com.m3sv.droidupnp.data.Directory
import com.m3sv.droidupnp.presentation.base.BaseViewModel
import com.m3sv.droidupnp.upnp.UpnpManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.droidupnp.view.DeviceDisplay
import org.droidupnp.view.DeviceType
import timber.log.Timber
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val defaultUpnpManager: UpnpManager) :
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
                onNext = { renderer ->
                    Timber.d("Found Renderer: ${renderer.displayString}")
                    renderers += DeviceDisplay(renderer, false, DeviceType.RENDERER)
                    renderersObservable.postValue(renderers)
                },
                onError = errorHandler
            )

        discoveryDisposable += contentDirectoryDiscoveryObservable
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onNext = { contentDirectory ->
                    Timber.d("Found Content Directory: ${contentDirectory.displayString}")
                    contentDirectories += DeviceDisplay(
                        contentDirectory,
                        false,
                        DeviceType.CONTENT_DIRECTORY
                    )

                    contentDirectoriesObservable.postValue(contentDirectories)
                }, onError = errorHandler
            )
    }

    fun resetDevices() {
        Timber.d("Resetting devices")
        renderers.clear()
        contentDirectories.clear()
    }

    fun resumeUpnp() {
        Timber.d("Resuming UPnP controller")
        resumeUpnpController()
        resumeRendererUpdate()
    }

    fun pauseUpnp() {
        Timber.d("Pausing UPnP controller")
        pauseRendererUpdate()
    }
}
