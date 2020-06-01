package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import org.fourthline.cling.UpnpService
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.model.types.ServiceType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpnpServiceControllerImpl @Inject constructor(private val upnpService: UpnpService) :
    UpnpServiceController, RendererServiceFinder {

    override val contentDirectoryDiscovery: ContentDirectoryDiscovery =
        ContentDirectoryDiscovery(this, upnpService)

    override val rendererDiscovery: RendererDiscovery = RendererDiscovery(this, upnpService)

    override var selectedRenderer: UpnpDevice? = null

    override var selectedContentDirectory: UpnpDevice? = null

    init {
        start()
    }

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
        upnpService.addListenerSafe(rendererDiscovery.browsingRegistryListener)
        upnpService.addListenerSafe(contentDirectoryDiscovery.browsingRegistryListener)
    }

    override fun stop() {
        upnpService.removeListenerSafe(rendererDiscovery.browsingRegistryListener)
        upnpService.removeListenerSafe(contentDirectoryDiscovery.browsingRegistryListener)
    }

    override fun addDevice(localDevice: LocalDevice) {
        upnpService.registry?.addDevice(localDevice)
    }

    override fun removeDevice(localDevice: LocalDevice) {
        upnpService.registry?.removeDevice(localDevice)
    }

    override fun createContentDirectoryCommand(): ContentDirectoryCommand? =
        upnpService.controlPoint?.let { controlPoint ->
            ContentDirectoryCommand(
                controlPoint,
                this
            )
        }

    override fun findService(type: ServiceType): Service<*, *>? =
        selectedRenderer?.let { clingDevice -> (clingDevice as CDevice).device?.findService(type) }

    private fun UpnpService.addListenerSafe(registryListener: RegistryListener) {
        registry?.run {
            // Get ready for future device advertisements
            addListener(CRegistryListener(registryListener))

            // Now add all devices to the list we already know about
            devices?.forEach {
                registryListener.deviceAdded(CDevice(it))
            }
        }
    }

    private fun UpnpService.removeListenerSafe(registryListener: RegistryListener) {
        registry?.removeListener(CRegistryListener(registryListener))
    }
}
