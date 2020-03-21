package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import org.fourthline.cling.model.meta.LocalDevice
import javax.inject.Inject

class UpnpServiceControllerImpl @Inject constructor(override val serviceListener: UpnpServiceListener) :
    UpnpServiceController {

    override val contentDirectoryDiscovery: ContentDirectoryDiscovery =
        ContentDirectoryDiscovery(this)

    override val rendererDiscovery: RendererDiscovery = RendererDiscovery(this)

    override var selectedRenderer: UpnpDevice? = null

    override var selectedContentDirectory: UpnpDevice? = null

    override fun setSelectedContentDirectory(contentDirectory: UpnpDevice, force: Boolean) {
        if (!force
            && selectedContentDirectory != null
            && contentDirectory.equals(selectedContentDirectory)
        ) return

        this.selectedContentDirectory = contentDirectory
    }

    override fun setSelectedRenderer(renderer: UpnpDevice, force: Boolean) {
        // Skip if no change and no force
        if (!force
            && selectedRenderer != null
            && renderer.equals(selectedRenderer)
        ) return

        this.selectedRenderer = renderer
    }

    override fun start() {
        rendererDiscovery.resume(serviceListener)
        contentDirectoryDiscovery.resume(serviceListener)
        serviceListener.bindService()
    }

    override fun stop() {
        rendererDiscovery.pause(serviceListener)
        contentDirectoryDiscovery.pause(serviceListener)
        serviceListener.unbindService()
    }

    override fun addDevice(localDevice: LocalDevice) {
        serviceListener.upnpService?.registry?.addDevice(localDevice)
    }

    override fun removeDevice(localDevice: LocalDevice) {
        serviceListener.upnpService?.registry?.removeDevice(localDevice)
    }

    override fun createRendererCommand(upnpInnerState: UpnpInnerState): RendererCommand? =
        serviceListener.controlPoint?.let { controlPoint ->
            RendererCommand(
                this,
                controlPoint,
                upnpInnerState
            )
        }

    override fun createContentDirectoryCommand(): ContentDirectoryCommand? =
        serviceListener.controlPoint?.let { controlPoint ->
            ContentDirectoryCommand(
                controlPoint,
                this
            )
        }
}
