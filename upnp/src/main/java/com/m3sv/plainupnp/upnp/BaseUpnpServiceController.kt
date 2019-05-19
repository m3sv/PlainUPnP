package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice

abstract class BaseUpnpServiceController protected constructor() : UpnpServiceController {

    override val contentDirectoryDiscovery: ContentDirectoryDiscovery = ContentDirectoryDiscovery(this)

    override val rendererDiscovery: RendererDiscovery = RendererDiscovery(this)

    override var selectedRenderer: UpnpDevice? = null

    override var selectedContentDirectory: UpnpDevice? = null

    override fun setSelectedContentDirectory(contentDirectory: UpnpDevice, force: Boolean) {
        // Skip if no change and no force
        if (!force && this.selectedContentDirectory != null && this.selectedContentDirectory!!.equals(contentDirectory))
            return

        this.selectedContentDirectory = contentDirectory
    }

    override fun setSelectedRenderer(renderer: UpnpDevice, force: Boolean) {
        // Skip if no change and no force
        if (!force && this.selectedRenderer != null && this.selectedRenderer!!.equals(renderer))
            return

        this.selectedRenderer = renderer
    }

    // Pause the service
    override fun pause() {
        rendererDiscovery.pause(serviceListener)
        contentDirectoryDiscovery.pause(serviceListener)
    }

    // Resume the service
    override fun resume() {
        rendererDiscovery.resume(serviceListener)
        contentDirectoryDiscovery.resume(serviceListener)
    }
}