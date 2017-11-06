package org.droidupnp.model.upnp;

public interface IDeviceDiscoveryObserver {

	public void addedDevice(IUPnPDevice device);

	public void removedDevice(IUPnPDevice device);
}
