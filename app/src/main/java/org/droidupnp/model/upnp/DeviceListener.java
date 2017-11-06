package org.droidupnp.model.upnp;


import org.droidupnp.controller.upnp.IUPnPServiceController;

public class DeviceListener {

    // UPNP device listener
    private RendererDiscovery rendererDiscovery = null;
    private ContentDirectoryDiscovery contentDirectoryDiscovery = null;

    public DeviceListener(IUPnPServiceController controller, IServiceListener serviceListener) {
        rendererDiscovery = new RendererDiscovery(controller, serviceListener);
        contentDirectoryDiscovery = new ContentDirectoryDiscovery(controller, serviceListener);
    }

    public RendererDiscovery getRendererDiscovery() {
        return rendererDiscovery;
    }

    public ContentDirectoryDiscovery getContentDirectoryDiscovery() {
        return contentDirectoryDiscovery;
    }
}
