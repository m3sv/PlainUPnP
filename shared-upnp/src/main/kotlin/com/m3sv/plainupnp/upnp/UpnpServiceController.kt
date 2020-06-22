package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.discovery.device.ContentDirectoryDiscovery
import com.m3sv.plainupnp.upnp.discovery.device.RendererDiscovery

interface UpnpServiceController {

    var selectedRenderer: UpnpDevice?

    var selectedContentDirectory: UpnpDevice?

    val contentDirectoryDiscovery: ContentDirectoryDiscovery

    val rendererDiscovery: RendererDiscovery

    fun setSelectedRenderer(renderer: UpnpDevice, force: Boolean)

    fun setSelectedContentDirectory(contentDirectory: UpnpDevice, force: Boolean)

    fun start()

    fun stop()

    fun createContentDirectoryCommand(): ContentDirectoryCommand?

}
