package com.m3sv.droidupnp.upnp

import com.m3sv.droidupnp.upnp.observer.ContentDirectoryDiscoveryObservable
import com.m3sv.droidupnp.upnp.observer.RendererDiscoveryObservable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import org.droidupnp.controller.upnp.IUPnPServiceController
import org.droidupnp.model.upnp.IDeviceDiscoveryObserver
import org.droidupnp.model.upnp.IFactory
import org.droidupnp.model.upnp.IUPnPDevice
import timber.log.Timber
import java.util.*


class UPnPManager constructor(val controller: IUPnPServiceController, val factory: IFactory) : Observable(), IDeviceDiscoveryObserver, Observer {
    lateinit var deviceDiscoveryDisposable: CompositeDisposable

    fun addObservers() = controller.run {
        deviceDiscoveryDisposable = CompositeDisposable().apply {
            add(RendererDiscoveryObservable(rendererDiscovery).subscribeBy(
                    onNext = { Timber.d("Found Renderer: ${it.friendlyName}") },
                    onError = { Timber.e("Error while discovering renderer: ${it.message}") }))

            add(ContentDirectoryDiscoveryObservable(contentDirectoryDiscovery).subscribeBy(
                    onNext = {
                        Timber.d("Found Content Directory: ${it.friendlyName}")
                    }, onError = { Timber.e("Error while discovering content directory: ${it.message}") }))
        }
        addSelectedContentDirectoryObserver(this@UPnPManager)
    }

    fun removeObservers() = controller.run {
        deviceDiscoveryDisposable.takeUnless { it.isDisposed }?.dispose()
        delSelectedContentDirectoryObserver(this@UPnPManager)
    }

    override fun addedDevice(device: IUPnPDevice?) {
        Timber.d("Found new device: ${device?.friendlyName}")
    }

    override fun removedDevice(device: IUPnPDevice?) {
    }

    override fun update(o: Observable?, arg: Any?) {
    }
}