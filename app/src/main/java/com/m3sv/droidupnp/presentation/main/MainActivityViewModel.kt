package com.m3sv.droidupnp.presentation.main

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.m3sv.droidupnp.upnp.UPnPManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.droidupnp.view.DeviceDisplay
import org.droidupnp.view.DeviceType
import timber.log.Timber

class MainActivityViewModel(private val manager: UPnPManager) : ViewModel() {
    val contentDirectoriesObservable = MutableLiveData<Set<DeviceDisplay>>()
    val renderersObservable = MutableLiveData<Set<DeviceDisplay>>()

    private lateinit var discoveryDisposable: CompositeDisposable

    private val renderers = hashSetOf<DeviceDisplay>()
    private val contentDirectories = hashSetOf<DeviceDisplay>()

    fun resumeController() {
        discoveryDisposable = CompositeDisposable()
        manager.run {
            controller.resume()
            discoveryDisposable += rendererDiscoveryObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onNext = { renderer ->
                                Timber.d("Found Renderer: ${renderer.friendlyName}")
                                renderers += DeviceDisplay(renderer, false, DeviceType.RENDERER)
                                renderersObservable.value = renderers
                            },
                            onError = { Timber.e("Error while discovering renderer: ${it.message}") })
            discoveryDisposable += contentDirectoryDiscoveryObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                            onNext = { contentDirectory ->
                                Timber.d("Found Content Directory: ${contentDirectory.friendlyName}")
                                contentDirectories += DeviceDisplay(contentDirectory, false, DeviceType.CONTENT_DIRECTORY)
                                contentDirectoriesObservable.value = contentDirectories
                            }, onError = { Timber.e("Error while discovering renderer: ${it.message}") })
            addObservers()
        }
    }


    fun pauseController() = manager.controller.run {
        pause()
        discoveryDisposable.takeUnless { it.isDisposed }?.dispose()
        manager.removeObservers()
    }

    fun refreshServiceListener() = manager.controller.serviceListener?.refresh()
}
