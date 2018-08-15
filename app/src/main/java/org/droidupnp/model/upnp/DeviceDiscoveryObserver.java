package org.droidupnp.model.upnp;

public interface DeviceDiscoveryObserver {

	public void addedDevice(IUPnPDevice device);

	public void removedDevice(IUPnPDevice device);
}
