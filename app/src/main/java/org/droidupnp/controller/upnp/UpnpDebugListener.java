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

import android.util.Log;

import org.droidupnp.model.upnp.IRegistryListener;
import org.droidupnp.model.upnp.IUpnpDevice;

public class UpnpDebugListener implements IRegistryListener {

    protected static final String TAG = "ClingDebugListener";

    @Override
    public void deviceAdded(final IUpnpDevice device) {
        Log.i(TAG, "New device detected : " + device.getDisplayString());
    }

    @Override
    public void deviceRemoved(final IUpnpDevice device) {
        Log.i(TAG, "Device removed : " + device.getDisplayString());
    }
}