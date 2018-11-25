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

    private var contentDirectoryObservable: CObservable = CObservable()

    override val contentDirectoryDiscovery: ContentDirectoryDiscovery by lazy {
        ContentDirectoryDiscovery(this)
    }
    override val rendererDiscovery: RendererDiscovery by lazy {
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