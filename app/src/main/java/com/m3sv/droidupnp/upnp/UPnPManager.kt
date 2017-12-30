package com.m3sv.droidupnp.upnp

import com.m3sv.droidupnp.upnp.observer.ContentDirectoryDiscoveryObservable
import com.m3sv.droidupnp.upnp.observer.RendererDiscoveryObservable
import io.reactivex.disposables.CompositeDisposable
import org.droidupnp.controller.upnp.IUPnPServiceController
import org.droidupnp.model.upnp.IDeviceDiscoveryObserver
import org.droidupnp.model.upnp.IFactory
import org.droidupnp.model.upnp.IUPnPDevice
import timber.log.Timber
import java.util.*


class UPnPManager constructor(val controller: IUPnPServiceController, val factory: IFactory) : Observable(), IDeviceDiscoveryObserver, Observer {
    val rendererDiscoveryObservable = RendererDiscoveryObservable(controller.rendererDiscovery)
    val contentDirectoryDiscoveryObservable = ContentDirectoryDiscoveryObservable(controller.contentDirectoryDiscovery)

    fun addObservers() = controller.run {
        addSelectedContentDirectoryObserver(this@UPnPManager)
    }

    fun removeObservers() = controller.run {
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