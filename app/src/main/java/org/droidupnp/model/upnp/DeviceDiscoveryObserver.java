package org.droidupnp.model.upnp;

import com.m3sv.droidupnp.data.UpnpDeviceEvent;

public interface DeviceDiscoveryObserver {

    void addedDevice(UpnpDeviceEvent device);

    void removedDevice(UpnpDeviceEvent device);
}
