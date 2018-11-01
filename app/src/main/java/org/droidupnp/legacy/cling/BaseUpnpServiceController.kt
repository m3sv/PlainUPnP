/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien></aurelien>@chabot.fr>
 *
 *
 * This file is part of DroidUPNP.
 *
 *
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */

package org.droidupnp.legacy.cling

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.UpnpServiceController
import com.m3sv.plainupnp.upnp.discovery.ContentDirectoryDiscovery
import com.m3sv.plainupnp.upnp.discovery.RendererDiscovery

import org.droidupnp.legacy.CObservable

import java.util.Observer

abstract class BaseUpnpServiceController protected constructor() : UpnpServiceController {

    protected var renderer: UpnpDevice? = null
    protected var contentDirectory: UpnpDevice? = null

    protected var contentDirectoryObservable: CObservable

    private val contentDirectoryDiscovery: ContentDirectoryDiscovery
    private val rendererDiscovery: RendererDiscovery

    override fun getContentDirectoryDiscovery(): ContentDirectoryDiscovery {
        return contentDirectoryDiscovery
    }

    override fun getRendererDiscovery(): RendererDiscovery {
        return rendererDiscovery
    }

    // todo cleanup this
    init {
        contentDirectoryObservable = CObservable()

        contentDirectoryDiscovery = ContentDirectoryDiscovery(this)
        rendererDiscovery = RendererDiscovery(this)
    }

    override fun setSelectedRenderer(renderer: UpnpDevice) {
        setSelectedRenderer(renderer, false)
    }

    override fun setSelectedRenderer(renderer: UpnpDevice?, force: Boolean) {
        // Skip if no change and no force
        if (!force && renderer != null && this.renderer != null && this.renderer!!.equals(renderer))
            return

        this.renderer = renderer
    }

    override fun setSelectedContentDirectory(contentDirectory: UpnpDevice) {
        setSelectedContentDirectory(contentDirectory, false)
    }

    override fun setSelectedContentDirectory(contentDirectory: UpnpDevice?, force: Boolean) {
        // Skip if no change and no force
        if (!force && contentDirectory != null && this.contentDirectory != null
            && this.contentDirectory!!.equals(contentDirectory))
            return

        this.contentDirectory = contentDirectory
        contentDirectoryObservable.notifyAllObservers()
    }

    override fun getSelectedRenderer(): UpnpDevice? {
        return renderer
    }

    override fun getSelectedContentDirectory(): UpnpDevice? {
        return contentDirectory
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