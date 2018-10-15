/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * <p>
 * This file is part of DroidUPNP.
 * <p>
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.droidupnp.model.upnp;

import com.m3sv.droidupnp.data.UpnpDevice;

import org.droidupnp.controller.upnp.UpnpServiceController;

public class ContentDirectoryDiscovery extends DeviceDiscovery {

    protected static final String TAG = "ContentDirectoryDeviceFragment";

    public ContentDirectoryDiscovery(UpnpServiceController controller, IServiceListener serviceListener) {
        super(controller, serviceListener);
    }

    @Override
    protected ICallableFilter getCallableFilter() {
        return new CallableContentDirectoryFilter();
    }

    @Override
    protected boolean isSelected(UpnpDevice device) {
        return controller != null
                && controller.getSelectedContentDirectory() != null
                && device.equals(controller.getSelectedContentDirectory());
    }

    @Override
    protected void select(UpnpDevice device) {
        select(device, false);
    }

    @Override
    protected void select(UpnpDevice device, boolean force) {
        controller.setSelectedContentDirectory(device, force);
    }

    @Override
    protected void removed(UpnpDevice d) {
        if (controller != null && controller.getSelectedContentDirectory() != null
                && d.equals(controller.getSelectedContentDirectory()))
            controller.setSelectedContentDirectory(null);
    }
}
