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

import android.util.Log;

import org.droidupnp.controller.upnp.IUPnPServiceController;

import java.util.ArrayList;
import java.util.Collection;

import timber.log.Timber;

public abstract class DeviceDiscovery {

    protected static final String TAG = "DeviceDiscovery";

    private final BrowsingRegistryListener browsingRegistryListener;

    protected boolean extendedInformation;

    private final ArrayList<IDeviceDiscoveryObserver> observerList;

    protected final IUPnPServiceController controller;

    public DeviceDiscovery(IUPnPServiceController controller, IServiceListener serviceListener, boolean extendedInformation) {
        this.controller = controller;
        browsingRegistryListener = new BrowsingRegistryListener();
        this.extendedInformation = extendedInformation;
        observerList = new ArrayList<>();
    }

    public DeviceDiscovery(IUPnPServiceController controller, IServiceListener serviceListener) {
        this(controller, serviceListener, false);
    }

    public void resume(IServiceListener serviceListener) {
        serviceListener.addListener(browsingRegistryListener);
    }

    public void pause(IServiceListener serviceListener) {
        serviceListener.removeListener(browsingRegistryListener);
    }

    public class BrowsingRegistryListener implements IRegistryListener {

        @Override
        public void deviceAdded(final IUPnPDevice device) {
            Log.v(TAG, "New device detected : " + device.getDisplayString());

            if (device.isFullyHydrated() && filter(device)) {
                if (isSelected(device)) {
                    Log.i(TAG, "Reselect device to refresh it");
                    select(device, true);
                }

                notifyAdded(device);
            }
        }

        @Override
        public void deviceRemoved(final IUPnPDevice device) {
            Log.v(TAG, "Device removed : " + device.getFriendlyName());

            if (filter(device)) {
                if (isSelected(device)) {
                    Log.i(TAG, "Selected device have been removed");
                    removed(device);
                }

                notifyRemoved(device);
            }
        }
    }

    public void addObserver(IDeviceDiscoveryObserver o) {
        observerList.add(o);

        final Collection<IUPnPDevice> upnpDevices = controller.getServiceListener()
                .getFilteredDeviceList(getCallableFilter());
        for (IUPnPDevice d : upnpDevices)
            o.addedDevice(d);
    }

    public void removeObserver(IDeviceDiscoveryObserver o) {
        observerList.remove(o);
    }

    public void notifyAdded(IUPnPDevice device) {
        for (IDeviceDiscoveryObserver o : observerList)
            o.addedDevice(device);
    }

    public void notifyRemoved(IUPnPDevice device) {
        for (IDeviceDiscoveryObserver o : observerList)
            o.removedDevice(device);
    }

    /**
     * Filter device you want to add to this device list fragment
     *
     * @param device the device to test
     * @return add it or not
     * @throws Exception
     */
    protected boolean filter(IUPnPDevice device) {
        ICallableFilter filter = getCallableFilter();
        filter.setDevice(device);
        try {
            return filter.call();
        } catch (Exception e) {
            Timber.e(e);
        }
        return false;
    }

    /**
     * Get a callable device filter
     *
     * @return
     */
    protected abstract ICallableFilter getCallableFilter();

    /**
     * Filter to know if device is selected
     *
     * @param d
     * @return
     */
    protected abstract boolean isSelected(IUPnPDevice d);

    /**
     * Select a device
     *
     * @param device
     */
    protected abstract void select(IUPnPDevice device);

    /**
     * Select a device
     *
     * @param device
     * @param force
     */
    protected abstract void select(IUPnPDevice device, boolean force);

    /**
     * Callback when device removed
     *
     * @param d
     */
    protected abstract void removed(IUPnPDevice d);
}
