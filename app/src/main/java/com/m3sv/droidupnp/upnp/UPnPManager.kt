package com.m3sv.droidupnp.upnp

import com.m3sv.droidupnp.upnp.observer.RendererObserver
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import org.droidupnp.controller.upnp.IUPnPServiceController
import org.droidupnp.model.upnp.IDeviceDiscoveryObserver
import org.droidupnp.model.upnp.IFactory
import org.droidupnp.model.upnp.IUPnPDevice
import timber.log.Timber
import java.util.*


class UPnPManager constructor(val controller: IUPnPServiceController, val factory: IFactory) : Observable(), IDeviceDiscoveryObserver, Observer {
    var rendererDisposable: Disposable? = null

    fun addObservers() = controller.run {
        //        rendererDiscovery.addObserver(this@UPnPManager)

        rendererDisposable = RendererObserver(rendererDiscovery).subscribeBy(
                onNext = { Timber.d("Found Renderer: ${it.friendlyName}") },
                onError = { Timber.e("Error while discovering renders: ${it.message}") })
        contentDirectoryDiscovery.addObserver(this@UPnPManager)
        addSelectedContentDirectoryObserver(this@UPnPManager)
    }

    fun removeObservers() = controller.run {
        rendererDisposable?.dispose()
        contentDirectoryDiscovery.removeObserver(this@UPnPManager)
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