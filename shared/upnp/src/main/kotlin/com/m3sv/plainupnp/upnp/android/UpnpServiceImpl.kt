package com.m3sv.plainupnp.upnp.android;

import android.content.Context;

import org.fourthline.cling.UpnpService;
import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.controlpoint.ControlPoint;
import org.fourthline.cling.controlpoint.ControlPointImpl;
import org.fourthline.cling.protocol.ProtocolFactory;
import org.fourthline.cling.protocol.ProtocolFactoryImpl;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.registry.RegistryImpl;
import org.fourthline.cling.registry.RegistryListener;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;
import org.fourthline.cling.transport.RouterImpl;
import org.seamless.util.Exceptions;

import timber.log.Timber;

public class UpnpServiceImpl implements UpnpService {
    protected final UpnpServiceConfiguration configuration;
    protected final ControlPoint controlPoint;
    protected final ProtocolFactory protocolFactory;
    protected final Registry registry;
    protected final Router router;

    public UpnpServiceImpl(UpnpServiceConfiguration configuration, Context context, RegistryListener... registryListeners) {
        this.configuration = configuration;
        Timber.i(">>> Starting UPnP service...");
        Timber.i("Using configuration: %s", this.getConfiguration().getClass().getName());
        this.protocolFactory = this.createProtocolFactory();
        this.registry = this.createRegistry(this.protocolFactory);

        for (RegistryListener registryListener : registryListeners) {
            this.registry.addListener(registryListener);
        }

        this.router = new AndroidRouter(configuration, this.protocolFactory, context);

        try {
            this.router.enable();
        } catch (RouterException var7) {
            throw new RuntimeException("Enabling network router failed: " + var7, var7);
        }

        this.controlPoint = this.createControlPoint(this.protocolFactory, this.registry);
        Timber.i("<<< UPnP service started successfully");
    }

    protected ProtocolFactory createProtocolFactory() {
        return new ProtocolFactoryImpl(this);
    }

    protected Registry createRegistry(ProtocolFactory protocolFactory) {
        return new RegistryImpl(this);
    }

    protected Router createRouter(ProtocolFactory protocolFactory, Registry registry) {
        return new RouterImpl(this.getConfiguration(), protocolFactory);
    }

    protected ControlPoint createControlPoint(ProtocolFactory protocolFactory, Registry registry) {
        return new ControlPointImpl(this.getConfiguration(), protocolFactory, registry);
    }

    public UpnpServiceConfiguration getConfiguration() {
        return this.configuration;
    }

    public ControlPoint getControlPoint() {
        return this.controlPoint;
    }

    public ProtocolFactory getProtocolFactory() {
        return this.protocolFactory;
    }

    public Registry getRegistry() {
        return this.registry;
    }

    public Router getRouter() {
        return this.router;
    }

    public synchronized void shutdown() {
        this.shutdown(false);
    }

    protected void shutdown(boolean separateThread) {
        Runnable shutdown = () -> {
            Timber.i(">>> Shutting down UPnP service...");
            UpnpServiceImpl.this.shutdownRegistry();
            UpnpServiceImpl.this.shutdownRouter();
            UpnpServiceImpl.this.shutdownConfiguration();
            Timber.i("<<< UPnP service shutdown completed");
        };
        if (separateThread) {
            (new Thread(shutdown)).start();
        } else {
            shutdown.run();
        }

    }

    protected void shutdownRegistry() {
        this.getRegistry().shutdown();
    }

    protected void shutdownRouter() {
        try {
            this.getRouter().shutdown();
        } catch (RouterException var3) {
            Throwable cause = Exceptions.unwrap(var3);
            if (cause instanceof InterruptedException) {
                Timber.e(cause, "Router shutdown was interrupted: %s", var3);
            } else {
                Timber.e(cause, "Router error on shutdown: " + var3);
            }
        }

    }

    protected void shutdownConfiguration() {
        this.getConfiguration().shutdown();
    }
}
