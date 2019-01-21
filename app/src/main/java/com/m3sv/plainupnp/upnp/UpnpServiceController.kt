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

package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.upnp.discovery.ContentDirectoryDiscovery
import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.discovery.RendererDiscovery
import org.fourthline.cling.model.meta.LocalDevice

import java.util.Observer

interface UpnpServiceController {

    var selectedRenderer: UpnpDevice?

    var selectedContentDirectory: UpnpDevice?

    val serviceListener: ServiceListener

    val contentDirectoryDiscovery: ContentDirectoryDiscovery

    val rendererDiscovery: RendererDiscovery

    fun setSelectedRenderer(renderer: UpnpDevice, force: Boolean)

    fun setSelectedContentDirectory(contentDirectory: UpnpDevice, force: Boolean)

    // Pause the service
    fun pause()

    // Resume the service
    fun resume()

    fun addDevice(localDevice: LocalDevice)

    fun removeDevice(localDevice: LocalDevice)
}
