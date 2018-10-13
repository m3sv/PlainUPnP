package org.droidupnp.model.upnp;

import com.m3sv.droidupnp.data.IUpnpDevice;

public interface DeviceDiscoveryObserver {

    void addedDevice(IUpnpDevice device);

    void removedDevice(IUpnpDevice device);
}
