package org.droidupnp.model.upnp;

public class CallableContentDirectoryFilter implements ICallableFilter {

	private IUPnPDevice device;

	public void setDevice(IUPnPDevice device)
	{
		this.device = device;
	}

	@Override
	public Boolean call() throws Exception
	{
		return device.asService("ContentDirectory");
	}
}
