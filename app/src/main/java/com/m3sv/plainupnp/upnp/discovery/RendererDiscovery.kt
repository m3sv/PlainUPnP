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

package com.m3sv.plainupnp.upnp.discovery

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.UpnpServiceController
import org.droidupnp.legacy.upnp.CallableRendererFilter
import org.droidupnp.legacy.upnp.ICallableFilter

class RendererDiscovery(controller: UpnpServiceController) :
    DeviceDiscovery(controller) {

    override fun getCallableFilter(): ICallableFilter {
        return CallableRendererFilter()
    }

    override fun isSelected(device: UpnpDevice): Boolean {
        return if (controller != null && controller.selectedRenderer != null)
            device.equals(controller.selectedRenderer)
        else false
    }

    override fun select(device: UpnpDevice) {
        select(device, false)
    }

    override fun select(device: UpnpDevice, force: Boolean) {
        controller.setSelectedRenderer(device, force)
    }

    override fun removed(d: UpnpDevice) {
        if (controller != null
            && controller.selectedRenderer != null
            && d.equals(controller.selectedRenderer))
            controller.selectedRenderer = null
    }
}