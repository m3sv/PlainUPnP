package com.m3sv.plainupnp.presentation.main

import android.arch.lifecycle.MutableLiveData
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.Directory
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.upnp.UpnpManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
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
        disposables += selectedDirectoryObservable
            .subscribeBy(
                onNext = {
                    currentDirectory = it
                }, onError = Timber::e
            )

        discoveryDisposable += rendererDiscoveryObservable
            .subscribeOn(Schedulers.io())
            .debounce(1000, TimeUnit.MILLISECONDS)
            .subscribeBy(
                onNext = renderers::postValue,
                onError = errorHandler
            )

        discoveryDisposable += contentDirectoryDiscoveryObservable
            .subscribeOn(Schedulers.io())
            .debounce(1000, TimeUnit.MILLISECONDS)
            .subscribeBy(
                onNext = contentDirectories::postValue, onError = errorHandler
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
