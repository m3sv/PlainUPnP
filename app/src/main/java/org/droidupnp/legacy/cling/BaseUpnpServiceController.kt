package org.droidupnp.legacy.cling

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.UpnpServiceController
import com.m3sv.plainupnp.upnp.discovery.ContentDirectoryDiscovery
import com.m3sv.plainupnp.upnp.discovery.RendererDiscovery
import org.droidupnp.legacy.CObservable
import java.util.*

abstract class BaseUpnpServiceController protected constructor() : UpnpServiceController {

    private var contentDirectoryObservable: CObservable = CObservable()

    override val contentDirectoryDiscovery: ContentDirectoryDiscovery by lazy(mode = LazyThreadSafetyMode.NONE) {
        ContentDirectoryDiscovery(this)
    }
    override val rendererDiscovery: RendererDiscovery by lazy(mode = LazyThreadSafetyMode.NONE) {
        RendererDiscovery(this)
    }

    override var selectedRenderer: UpnpDevice? = null

    override var selectedContentDirectory: UpnpDevice? = null

    override fun setSelectedContentDirectory(contentDirectory: UpnpDevice, force: Boolean) {
        // Skip if no change and no force
        if (!force && this.selectedContentDirectory != null && this.selectedContentDirectory!!.equals(
                contentDirectory
            ))
            return

        this.selectedContentDirectory = contentDirectory
        contentDirectoryObservable.notifyAllObservers()
    }

    override fun setSelectedRenderer(renderer: UpnpDevice, force: Boolean) {
        // Skip if no change and no force
        if (!force && this.selectedRenderer != null && this.selectedRenderer!!.equals(renderer))
            return

        this.selectedRenderer = renderer
    }

    override fun addSelectedContentDirectoryObserver(o: Observer) {
        contentDirectoryObservable.addObserver(o)
    }

    override fun delSelectedContentDirectoryObserver(o: Observer) {
        contentDirectoryObservable.deleteObserver(o)
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