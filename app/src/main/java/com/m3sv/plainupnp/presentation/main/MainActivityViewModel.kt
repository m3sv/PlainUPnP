package com.m3sv.plainupnp.presentation.main

import android.arch.lifecycle.MutableLiveData
import com.m3sv.plainupnp.common.utils.disposeBy
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.Directory
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.upnp.UpnpManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val defaultUpnpManager: UpnpManager
) : BaseViewModel(), UpnpManager by defaultUpnpManager {

    var currentDirectory: Directory? = null

    val contentDirectories = MutableLiveData<Set<DeviceDisplay>>()

    val renderers = MutableLiveData<Set<DeviceDisplay>>()

    private val discoveryDisposable: CompositeDisposable = CompositeDisposable()

    private val errorHandler: (Throwable) -> Unit =
        { Timber.e("Exception during discovery: ${it.message}") }

    init {
        selectedDirectoryObservable.subscribeBy(
            onNext = { currentDirectory = it },
            onError = Timber::e
        ).disposeBy(disposables)

        rendererDiscovery.subscribeBy(
            onNext = renderers::postValue,
            onError = errorHandler
        ).disposeBy(discoveryDisposable)

        contentDirectoryDiscovery.subscribeBy(
            onNext = contentDirectories::postValue,
            onError = errorHandler
        ).disposeBy(discoveryDisposable)
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
