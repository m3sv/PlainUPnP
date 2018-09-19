package org.droidupnp.model.upnp;

public interface DeviceDiscoveryObserver {

	public void addedDevice(IUpnpDevice device);

	public void removedDevice(IUpnpDevice device);
}
