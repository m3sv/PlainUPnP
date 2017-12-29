/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien@chabot.fr>
 * 
 * This file is part of DroidUPNP.
 * 
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.droidupnp.controller.upnp;

import java.util.Observer;

import org.droidupnp.model.upnp.ContentDirectoryDiscovery;
import org.droidupnp.model.upnp.IServiceListener;
import org.droidupnp.model.upnp.IUPnPDevice;
import org.droidupnp.model.upnp.RendererDiscovery;
import org.fourthline.cling.model.meta.LocalDevice;

import android.app.Activity;

public interface IUPnPServiceController {
	public void setSelectedRenderer(IUPnPDevice renderer);

	public void setSelectedRenderer(IUPnPDevice renderer, boolean force);

	public void setSelectedContentDirectory(IUPnPDevice contentDirectory);

	public void setSelectedContentDirectory(IUPnPDevice contentDirectory, boolean force);

	public IUPnPDevice getSelectedRenderer();

	public IUPnPDevice getSelectedContentDirectory();

	public void addSelectedRendererObserver(Observer o);

	public void delSelectedRendererObserver(Observer o);

	public void addSelectedContentDirectoryObserver(Observer o);

	public void delSelectedContentDirectoryObserver(Observer o);

	public IServiceListener getServiceListener();

	public ContentDirectoryDiscovery getContentDirectoryDiscovery();

	public RendererDiscovery getRendererDiscovery();

	// Pause the service
	public void pause();

	// Resume the service
	public void resume();

	public void addDevice(LocalDevice localDevice);
	public void removeDevice(LocalDevice localDevice);

}
