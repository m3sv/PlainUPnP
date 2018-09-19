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

package org.droidupnp.controller.upnp;

import java.util.Observer;

import org.droidupnp.model.upnp.ContentDirectoryDiscovery;
import org.droidupnp.model.upnp.IServiceListener;
import org.droidupnp.model.upnp.IUpnpDevice;
import org.droidupnp.model.upnp.RendererDiscovery;
import org.fourthline.cling.model.meta.LocalDevice;

public interface UpnpServiceController {
    void setSelectedRenderer(IUpnpDevice renderer);

    void setSelectedRenderer(IUpnpDevice renderer, boolean force);

    void setSelectedContentDirectory(IUpnpDevice contentDirectory);

    void setSelectedContentDirectory(IUpnpDevice contentDirectory, boolean force);

    IUpnpDevice getSelectedRenderer();

    IUpnpDevice getSelectedContentDirectory();

    void addSelectedRendererObserver(Observer o);

    void delSelectedRendererObserver(Observer o);

    void addSelectedContentDirectoryObserver(Observer o);

    void delSelectedContentDirectoryObserver(Observer o);

    IServiceListener getServiceListener();

    ContentDirectoryDiscovery getContentDirectoryDiscovery();

    RendererDiscovery getRendererDiscovery();

    // Pause the service
    void pause();

    // Resume the service
    void resume();

    void addDevice(LocalDevice localDevice);

    void removeDevice(LocalDevice localDevice);
}
