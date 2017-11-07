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

package org.droidupnp.controller.cling;

import android.content.Context;

import org.droidupnp.controller.upnp.IUPnPServiceController;
import org.droidupnp.model.cling.RendererState;
import org.droidupnp.model.upnp.ARendererState;
import org.droidupnp.model.upnp.IContentDirectoryCommand;
import org.droidupnp.model.upnp.IFactory;
import org.droidupnp.model.upnp.IRendererCommand;
import org.droidupnp.model.upnp.IRendererState;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.controlpoint.ControlPoint;

import javax.inject.Inject;

public class Factory implements IFactory {
    private final IUPnPServiceController controller;

    @Inject
    public Factory(IUPnPServiceController controller) {
        this.controller = controller;
    }

    @Override
    public IContentDirectoryCommand createContentDirectoryCommand() {
        AndroidUpnpService aus = ((ServiceListener) controller.getServiceListener()).getUpnpService();
        ControlPoint cp = null;
        if (aus != null)
            cp = aus.getControlPoint();
        if (cp != null)
            return new ContentDirectoryCommand(cp, controller);

        return null;
    }

    @Override
    public IRendererCommand createRendererCommand(IRendererState rs) {
        AndroidUpnpService aus = ((ServiceListener) controller.getServiceListener()).getUpnpService();
        ControlPoint cp = null;
        if (aus != null)
            cp = aus.getControlPoint();
        if (cp != null)
            return new RendererCommand(cp, (RendererState) rs);

        return null;
    }

    @Override
    public IUPnPServiceController createUpnpServiceController(Context ctx) {
        return controller;
    }

    @Override
    public ARendererState createRendererState() {
        return new RendererState();
    }
}
