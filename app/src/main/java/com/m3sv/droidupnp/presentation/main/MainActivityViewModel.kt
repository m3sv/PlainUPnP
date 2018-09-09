package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.MutableLiveData
import com.m3sv.droidupnp.presentation.base.BaseViewModel
import com.m3sv.droidupnp.upnp.UPnPManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.droidupnp.model.upnp.IUPnPDevice
import org.droidupnp.view.DeviceDisplay
import org.droidupnp.view.DeviceType
import timber.log.Timber
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val manager: UPnPManager) :
    BaseViewModel() {

    var lastFragmentTag: String? = null

    val contentDirectoriesObservable = MutableLiveData<Set<DeviceDisplay>>()

    val renderersObservable = MutableLiveData<Set<DeviceDisplay>>()

    private val discoveryDisposable: CompositeDisposable = CompositeDisposable()

    private val renderers = hashSetOf<DeviceDisplay>()

    private val contentDirectories = hashSetOf<DeviceDisplay>()

    private val errorHandler: (Throwable) -> Unit =
        { Timber.e("Exception during discovery: ${it.message}") }

    init {
        discoveryDisposable += manager.rendererDiscoveryObservable
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onNext = { renderer ->
                    Timber.d("Found Renderer: ${renderer.displayString}")
                    renderers += DeviceDisplay(renderer, false, DeviceType.RENDERER)
                    renderersObservable.postValue(renderers)
                },
                onError = errorHandler
            )

        discoveryDisposable += manager.contentDirectoryDiscoveryObservable
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
        renderers.clear()
        contentDirectories.clear()
    }

    fun addObservers() = manager.addObservers()

    fun removeObservers() = manager.removeObservers()

    fun resumeController() {
        manager.controller.resume()
    }

    fun pauseController() {
        with(manager.controller) {
            pause()
            serviceListener.serviceConnection.onServiceDisconnected(null)
        }
    }

    fun refreshServiceListener() = manager.controller.serviceListener?.refresh()

    fun navigateHome() {
        manager.browseHome()
    }

    fun selectDevice(device: IUPnPDevice?) {
        manager.selectDevice(device)
    }
}
