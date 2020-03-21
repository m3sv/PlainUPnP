package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import org.fourthline.cling.model.meta.LocalDevice

interface UpnpServiceController {

    var selectedRenderer: UpnpDevice?

    var selectedContentDirectory: UpnpDevice?

    val serviceListener: UpnpServiceListener

    val contentDirectoryDiscovery: ContentDirectoryDiscovery

    val rendererDiscovery: RendererDiscovery

    fun setSelectedRenderer(renderer: UpnpDevice, force: Boolean)

    fun setSelectedContentDirectory(contentDirectory: UpnpDevice, force: Boolean)

    fun start()

    fun stop()

    fun addDevice(localDevice: LocalDevice)

    fun removeDevice(localDevice: LocalDevice)

    fun createRendererCommand(upnpInnerState: UpnpInnerState): RendererCommand?

    fun createContentDirectoryCommand(): ContentDirectoryCommand?

}
