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

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import com.m3sv.common.Utils;
import com.m3sv.plainupnp.data.UpnpDevice;

import org.droidupnp.legacy.cling.CDevice;
import org.droidupnp.legacy.cling.CRegistryListener;
import org.droidupnp.legacy.mediaserver.MediaServer;
import org.droidupnp.legacy.upnp.ICallableFilter;
import org.droidupnp.legacy.upnp.IRegistryListener;
import org.droidupnp.legacy.upnp.IServiceListener;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.model.ValidationException;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.meta.Device;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import timber.log.Timber;

import static com.m3sv.plainupnp.common.PrefUtils.CONTENT_DIRECTORY_SERVICE;


@SuppressWarnings("rawtypes")
public class ServiceListener implements IServiceListener {

    private AndroidUpnpService upnpService;
    private ArrayList<IRegistryListener> waitingListener;

    private MediaServer mediaServer = null;
    private Context ctx;

    public ServiceListener(Context ctx) {
        waitingListener = new ArrayList<>();
        this.ctx = ctx;
    }

    @Override
    public void refresh() {
        upnpService.getControlPoint().search(new STAllHeader());
    }

    @Override
    public Collection<UpnpDevice> getDeviceList() {
        ArrayList<UpnpDevice> deviceList = new ArrayList<>();
        if (upnpService != null && upnpService.getRegistry() != null) {
            for (Device device : upnpService.getRegistry().getDevices()) {
                deviceList.add(new CDevice(device));
            }
        }
        return deviceList;
    }

    @Override
    public Collection<UpnpDevice> getFilteredDeviceList(ICallableFilter filter) {
        ArrayList<UpnpDevice> deviceList = new ArrayList<>();
        try {
            if (upnpService != null && upnpService.getRegistry() != null) {
                for (Device device : upnpService.getRegistry().getDevices()) {
                    UpnpDevice upnpDevice = new CDevice(device);
                    filter.setDevice(upnpDevice);

                    if (filter.call())
                        deviceList.add(upnpDevice);
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return deviceList;
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Timber.i("Connected service");
            upnpService = (AndroidUpnpService) service;

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
            if (sharedPref.getBoolean(CONTENT_DIRECTORY_SERVICE, true)) {
                try {
                    // Local content directory
                    if (mediaServer == null) {
                        mediaServer = new MediaServer(Utils.getLocalIpAddress(ctx), ctx);
                        mediaServer.start();
                    } else {
                        mediaServer.restart();
                    }
                    upnpService.getRegistry().addDevice(mediaServer.getDevice());
                } catch (UnknownHostException | ValidationException e1) {
                    Timber.e(e1, "Creating demo device failed");
                } catch (IOException e3) {
                    Timber.e(e3, "Starting http server failed");
                }
            } else if (mediaServer != null) {
                mediaServer.stop();
                mediaServer = null;
            }

            for (IRegistryListener registryListener : waitingListener) {
                addListenerSafe(registryListener);
            }

            // Search asynchronously for all devices, they will respond soon
            upnpService.getControlPoint().search();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Timber.i("Service disconnected");
            upnpService = null;
        }
    };

    @Override
    public ServiceConnection getServiceConnection() {
        return serviceConnection;
    }

    public AndroidUpnpService getUpnpService() {
        return upnpService;
    }

    @Override
    public void addListener(IRegistryListener registryListener) {
        if (upnpService != null)
            addListenerSafe(registryListener);
        else
            waitingListener.add(registryListener);
    }

    private void addListenerSafe(IRegistryListener registryListener) {
        assert upnpService != null;

        // Get ready for future device advertisements
        upnpService.getRegistry().addListener(new CRegistryListener(registryListener));

        // Now add all devices to the list we already know about
        for (Device device : upnpService.getRegistry().getDevices()) {
            registryListener.deviceAdded(new CDevice(device));
        }
    }

    @Override
    public void removeListener(IRegistryListener registryListener) {
        if (upnpService != null)
            removeListenerSafe(registryListener);
        else
            waitingListener.remove(registryListener);
    }

    private void removeListenerSafe(IRegistryListener registryListener) {
        assert upnpService != null;
        upnpService.getRegistry().removeListener(new CRegistryListener(registryListener));
    }

    @Override
    public void clearListener() {
        waitingListener.clear();
    }
}
