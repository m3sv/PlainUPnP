package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.cleanslate.UpnpServiceListener
import org.fourthline.cling.model.meta.LocalDevice

interface UpnpServiceController {

    var selectedRenderer: UpnpDevice?

    var selectedContentDirectory: UpnpDevice?

    val serviceListener: UpnpServiceListener

    val contentDirectoryDiscovery: ContentDirectoryDiscovery

    val rendererDiscovery: RendererDiscovery

    fun setSelectedRenderer(renderer: UpnpDevice, force: Boolean)

    fun setSelectedContentDirectory(contentDirectory: UpnpDevice, force: Boolean)

    fun pause()

    fun resume()

    fun addDevice(localDevice: LocalDevice)

    fun removeDevice(localDevice: LocalDevice)

    fun createRendererCommand(rendererStateObservable: UpnpRendererStateObservable?): RendererCommand?

    fun createContentDirectoryCommand(): ContentDirectoryCommand?

}
