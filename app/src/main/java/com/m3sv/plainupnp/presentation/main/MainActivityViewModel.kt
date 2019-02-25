package com.m3sv.plainupnp.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.m3sv.plainupnp.common.utils.disposeBy
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.Directory
import com.m3sv.plainupnp.data.upnp.RendererState
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.upnp.RenderedItem
import com.m3sv.plainupnp.upnp.UpnpManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
        private val defaultUpnpManager: UpnpManager
) : BaseViewModel(), UpnpManager by defaultUpnpManager {

    var currentDirectory: Directory? = null

    private val _contentDirectories = MutableLiveData<Set<DeviceDisplay>>()

    val contentDirectories: LiveData<Set<DeviceDisplay>> = _contentDirectories

    private val _renderers = MutableLiveData<Set<DeviceDisplay>>()

    val renderers: LiveData<Set<DeviceDisplay>> = _renderers

    private val _rendererState = MutableLiveData<RendererState>()

    val rendererState: LiveData<RendererState> = _rendererState

    private val _renderedNewItem = MutableLiveData<RenderedItem>()

    val renderedNewItem: LiveData<RenderedItem> = _renderedNewItem

    private val discoveryDisposable: CompositeDisposable = CompositeDisposable()

    private val errorHandler: (Throwable) -> Unit =
            { Timber.e("Exception during discovery: ${it.message}") }

    init {
        selectedDirectoryObservable
                .subscribeBy(
                        onNext = { currentDirectory = it },
                        onError = Timber::e
                ).disposeBy(disposables)

        rendererDiscovery
                .subscribe(_renderers::postValue, errorHandler)
                .disposeBy(discoveryDisposable)

        contentDirectoryDiscovery
                .subscribe(_contentDirectories::postValue, errorHandler)
                .disposeBy(discoveryDisposable)

        upnpRendererState
                .subscribe(_rendererState::postValue, errorHandler)
                .disposeBy(disposables)

        renderedItem
                .subscribe(_renderedNewItem::postValue, errorHandler)
                .disposeBy(disposables)
    }

    fun resumeUpnp() {
        Timber.d("Resuming UPnP upnpServiceController")
        resumeRendererUpdate()
    }

    fun pauseUpnp() {
        Timber.d("Pausing UPnP upnpServiceController")
        pauseRendererUpdate()
    }
}
