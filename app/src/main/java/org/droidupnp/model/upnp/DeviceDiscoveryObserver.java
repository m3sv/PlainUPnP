package org.droidupnp.model.upnp;

public interface DeviceDiscoveryObserver {

    void addedDevice(IUpnpDevice device);

    void removedDevice(IUpnpDevice device);
}
