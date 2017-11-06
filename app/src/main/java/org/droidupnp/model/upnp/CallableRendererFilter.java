package org.droidupnp.model.upnp;

public class CallableRendererFilter implements ICallableFilter {

	private IUPnPDevice device;

	public void setDevice(IUPnPDevice device)
	{
		this.device = device;
	}

	@Override
	public Boolean call() throws Exception
	{
		return device.asService("RenderingControl");
	}
}
