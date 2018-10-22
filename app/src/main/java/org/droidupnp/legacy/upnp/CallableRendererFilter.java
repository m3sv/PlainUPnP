package org.droidupnp.legacy.upnp;

import com.m3sv.plainupnp.data.UpnpDevice;

public class CallableRendererFilter implements ICallableFilter {

    private UpnpDevice device;

    public void setDevice(UpnpDevice device) {
        this.device = device;
    }

    @Override
    public Boolean call() {
        return device.asService("RenderingControl");
    }
}
