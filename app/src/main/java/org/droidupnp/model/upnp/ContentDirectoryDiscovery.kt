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

package org.droidupnp.model.upnp

import com.m3sv.droidupnp.data.UpnpDevice

import org.droidupnp.controller.upnp.UpnpServiceController

class ContentDirectoryDiscovery(
    controller: UpnpServiceController,
    serviceListener: IServiceListener?
) : DeviceDiscovery(controller, serviceListener) {

    override fun getCallableFilter(): ICallableFilter = CallableContentDirectoryFilter()

    override fun isSelected(device: UpnpDevice): Boolean {
        return (controller != null
                && controller.selectedContentDirectory != null
                && device.equals(controller.selectedContentDirectory))
    }

    override fun select(device: UpnpDevice) {
        select(device, false)
    }

    override fun select(device: UpnpDevice, force: Boolean) {
        controller.setSelectedContentDirectory(device, force)
    }

    override fun removed(d: UpnpDevice) {
        if (controller != null
            && controller.selectedContentDirectory != null
            && d.equals(controller.selectedContentDirectory))
            controller.selectedContentDirectory = null
    }
}
