package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.m3sv.droidupnp.presentation.base.BaseViewModel
import com.m3sv.droidupnp.upnp.RenderedItem
import com.m3sv.droidupnp.upnp.DefaultUpnpManager
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

class MainActivityViewModel @Inject constructor(private val upnpManager: UpnpManager) :
    BaseViewModel() {

    val contentDirectoriesObservable = MutableLiveData<Set<DeviceDisplay>>()

    val renderersObservable = MutableLiveData<Set<DeviceDisplay>>()

    val selectedDirectoryObservable = upnpManager.selectedDirectoryObservable

    val rendererState: LiveData<DefaultUpnpManager.RendererState> = upnpManager.rendererState

    val renderedItem: LiveData<RenderedItem> = upnpManager.renderedItem

    private val discoveryDisposable: CompositeDisposable = CompositeDisposable()

    private val renderers = LinkedHashSet<DeviceDisplay>()

    private val contentDirectories = LinkedHashSet<DeviceDisplay>()

    private val errorHandler: (Throwable) -> Unit =
        { Timber.e("Exception during discovery: ${it.message}") }

    init {
        discoveryDisposable += upnpManager.rendererDiscoveryObservable
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onNext = { renderer ->
                    Timber.d("Found Renderer: ${renderer.displayString}")
                    renderers += DeviceDisplay(renderer, false, DeviceType.RENDERER)
                    renderersObservable.postValue(renderers)
                },
                onError = errorHandler
            )

        discoveryDisposable += upnpManager.contentDirectoryDiscoveryObservable
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

    fun addObservers() = upnpManager.addObservers()

    fun removeObservers() = upnpManager.removeObservers()

    fun resumeUpnp() {
        Timber.d("Resuming UPnP controller")
        upnpManager.controller.resume()
        upnpManager.resumeRendererUpdate()
    }

    fun pauseUpnp() {
        Timber.d("Pausing UPnP controller")
        upnpManager.pauseRendererUpdate()
    }

    fun resumePlayback() {
        upnpManager.resumePlayback()
    }

    fun pausePlayback() {
        upnpManager.pausePlayback()
    }

    fun stopPlayback() {
        upnpManager.stopPlayback()
    }

    fun navigateHome() {
        Timber.d("Navigating home")
        upnpManager.browseHome()
    }

    fun selectContentDirectory(contentDirectory: IUpnpDevice?) {
        upnpManager.selectContentDirectory(contentDirectory)
    }

    fun selectRenderer(renderer: IUpnpDevice?) {
        upnpManager.selectRenderer(renderer)
    }

    fun pop() {
        upnpManager.browsePrevious()
    }

    fun pauseRendererCommand() {
        upnpManager.pauseRendererUpdate()
    }

    fun resumeRendererCommand() {
        upnpManager.resumeRendererUpdate()
    }

    fun moveTo(progress: Int, max: Int) {
        upnpManager.moveTo(progress, max)
    }

    fun playNext() {
        upnpManager.playNext()
    }

    fun playPrevious() {
        upnpManager.playPrevious()
    }
}
