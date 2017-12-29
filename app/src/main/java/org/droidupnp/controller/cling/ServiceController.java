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
import android.content.Intent;

import org.droidupnp.model.cling.UpnpService;
import org.droidupnp.model.cling.UpnpServiceController;
import org.fourthline.cling.model.meta.LocalDevice;

import javax.inject.Inject;

import timber.log.Timber;

public class ServiceController extends UpnpServiceController {

    private final ServiceListener upnpServiceListener;
    private Context context;

    @Inject
    ServiceController(Context context) {
        super();
        upnpServiceListener = new ServiceListener(context);
        this.context = context;
    }

    @Override
    protected void finalize() {
        pause();
    }

    @Override
    public ServiceListener getServiceListener() {
        return upnpServiceListener;
    }

    @Override
    public void pause() {
        super.pause();
        context.unbindService(upnpServiceListener.getServiceConnection());
    }

    @Override
    public void resume() {
        super.resume();
        // This will start the UPnP service if it wasn't already started
        Timber.d("Start upnp service");
        context.bindService(new Intent(context, UpnpService.class), upnpServiceListener.getServiceConnection(),
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void addDevice(LocalDevice localDevice) {
        upnpServiceListener.getUpnpService().getRegistry().addDevice(localDevice);
    }

    @Override
    public void removeDevice(LocalDevice localDevice) {
        upnpServiceListener.getUpnpService().getRegistry().removeDevice(localDevice);
    }

}
