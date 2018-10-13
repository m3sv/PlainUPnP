package org.droidupnp.model.upnp;

import com.m3sv.droidupnp.data.IUpnpDevice;

public class CallableRendererFilter implements ICallableFilter {

    private IUpnpDevice device;

    public void setDevice(IUpnpDevice device) {
        this.device = device;
    }

    @Override
    public Boolean call() {
        return device.asService("RenderingControl");
    }
}
