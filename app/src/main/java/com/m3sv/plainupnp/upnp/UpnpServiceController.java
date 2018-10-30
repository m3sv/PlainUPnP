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

package com.m3sv.plainupnp.upnp;

import com.m3sv.plainupnp.upnp.discovery.ContentDirectoryDiscovery;
import org.droidupnp.legacy.upnp.IServiceListener;
import com.m3sv.plainupnp.data.UpnpDevice;
import com.m3sv.plainupnp.upnp.discovery.RendererDiscovery;
import org.fourthline.cling.model.meta.LocalDevice;

import java.util.Observer;

public interface UpnpServiceController {
    void setSelectedRenderer(UpnpDevice renderer);

    void setSelectedRenderer(UpnpDevice renderer, boolean force);

    void setSelectedContentDirectory(UpnpDevice contentDirectory);

    void setSelectedContentDirectory(UpnpDevice contentDirectory, boolean force);

    UpnpDevice getSelectedRenderer();

    UpnpDevice getSelectedContentDirectory();

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
