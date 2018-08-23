package com.m3sv.droidupnp.upnp

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.m3sv.droidupnp.upnp.observers.ContentDirectoryDiscoveryObservable
import com.m3sv.droidupnp.upnp.observers.RendererDiscoveryObservable
import org.droidupnp.controller.upnp.IUPnPServiceController
import org.droidupnp.model.upnp.Factory
import org.droidupnp.model.upnp.DeviceDiscoveryObserver
import org.droidupnp.model.upnp.IContentDirectoryCommand
import org.droidupnp.model.upnp.IUPnPDevice
import org.droidupnp.model.upnp.didl.IDIDLItem
import timber.log.Timber
import java.util.*


class UPnPManager constructor(val controller: IUPnPServiceController, val factory: Factory) :
    DeviceDiscoveryObserver, Observer {
    val rendererDiscoveryObservable = RendererDiscoveryObservable(controller.rendererDiscovery)
    val contentDirectoryDiscoveryObservable =
        ContentDirectoryDiscoveryObservable(controller.contentDirectoryDiscovery)

    fun addObservers() = controller.run {
        rendererDiscovery.addObserver(this@UPnPManager)
        contentDirectoryDiscovery.addObserver(this@UPnPManager)
        addSelectedContentDirectoryObserver(this@UPnPManager)
    }

    fun removeObservers() = controller.run {
        rendererDiscovery.removeObserver(this@UPnPManager)
        contentDirectoryDiscovery.removeObserver(this@UPnPManager)
        delSelectedContentDirectoryObserver(this@UPnPManager)
    }

    private val _contentData = MutableLiveData<List<DIDLObjectDisplay>>()

    val contentData: LiveData<List<DIDLObjectDisplay>> = _contentData

    private val contentCallback: ContentCallback =
        object : ContentCallback {
            private var content: List<DIDLObjectDisplay>? = null

            override fun setContent(content: ArrayList<DIDLObjectDisplay>) {
                _contentData.postValue(content)
            }

            override fun run() {
                Timber.d("content size: ${content?.size ?: 0}")
            }
        }

    fun launchItem(item: IDIDLItem) {
        val rendererState = factory.createRendererState()
        val rendererCommand = factory.createRendererCommand(rendererState)

        rendererCommand?.run {
            resume()
            updateFull()
            launchItem(item)
        }
    }

    override fun addedDevice(device: IUPnPDevice?) {
        controller.selectedContentDirectory = device
        browseHome()
    }

    private fun browseHome() {
        browseTo("0")
    }

    fun browseTo(id: String, title: String?) {
        browseTo(id)
    }

    private fun browseTo(id: String) {
        factory.createContentDirectoryCommand()?.browse(id, null, contentCallback)
    }

    override fun removedDevice(device: IUPnPDevice?) {
    }

    override fun update(o: Observable?, arg: Any?) {
    }
}