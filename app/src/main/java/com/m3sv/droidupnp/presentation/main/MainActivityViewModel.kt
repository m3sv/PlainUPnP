package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.m3sv.droidupnp.presentation.base.BaseViewModel
import com.m3sv.droidupnp.upnp.UpnpManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.droidupnp.model.upnp.IUpnpDevice
import org.droidupnp.view.DeviceDisplay
import org.droidupnp.view.DeviceType
import timber.log.Timber
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val manager: UpnpManager) :
    BaseViewModel() {

    var lastFragmentTag: String? = null

    val contentDirectoriesObservable = MutableLiveData<Set<DeviceDisplay>>()

    val renderersObservable = MutableLiveData<Set<DeviceDisplay>>()

    val selectedDirectoryObservable = manager.selectedDirectoryObservable

    val rendererState: LiveData<UpnpManager.RendererState> = manager.rendererState

    private val discoveryDisposable: CompositeDisposable = CompositeDisposable()

    private val renderers = LinkedHashSet<DeviceDisplay>()

    private val contentDirectories = LinkedHashSet<DeviceDisplay>()

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
        Timber.d("Resetting devices")
        renderers.clear()
        contentDirectories.clear()
    }

    fun addObservers() = manager.addObservers()

    fun removeObservers() = manager.removeObservers()

    fun resumeController() {
        Timber.d("Resuming UPnP controller")
        manager.controller.resume()
    }

    fun pauseController() {
        Timber.d("Pausing UPnP controller")
//        with(manager.controller) {
//            pause()
//        }
    }

    fun refreshServiceListener() = manager.controller.serviceListener?.refresh()

    fun navigateHome() {
        Timber.d("Navigating home")
        manager.browseHome()
    }

    fun selectContentDirectory(contentDirectory: IUpnpDevice?) {
        manager.selectContentDirectory(contentDirectory)
    }

    fun selectRenderer(renderer: IUpnpDevice?) {
        manager.selectRenderer(renderer)
    }

    fun pop() {
        manager.browsePrevious()
    }
}
