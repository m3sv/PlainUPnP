package com.m3sv.plainupnp.upnp.android

import android.content.Context
import org.fourthline.cling.UpnpService
import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.controlpoint.ControlPointImpl
import org.fourthline.cling.protocol.ProtocolFactory
import org.fourthline.cling.protocol.ProtocolFactoryImpl
import org.fourthline.cling.registry.Registry
import org.fourthline.cling.registry.RegistryImpl
import org.fourthline.cling.registry.RegistryListener
import org.fourthline.cling.transport.Router
import org.fourthline.cling.transport.RouterException
import org.seamless.util.Exceptions
import timber.log.Timber

abstract class UpnpServiceImpl(
    private val configuration: UpnpServiceConfiguration,
    context: Context,
    vararg registryListeners: RegistryListener?,
) : UpnpService {

    private val _protocolFactory: ProtocolFactory by lazy { ProtocolFactoryImpl(this) }
    private val _registry: Registry by lazy { RegistryImpl(this) }
    private val _controlPoint: ControlPoint by lazy {
        ControlPointImpl(getConfiguration(), _protocolFactory, _registry)
    }

    private val router: Router

    override fun getProtocolFactory(): ProtocolFactory = _protocolFactory

    init {
        Timber.i(">>> Starting UPnP service...")

        for (registryListener in registryListeners) {
            _registry.addListener(registryListener)
        }

        router = AndroidRouter(configuration, _protocolFactory, context)

        try {
            router.enable()
        } catch (var7: RouterException) {
            throw RuntimeException("Enabling network router failed: $var7", var7)
        }
        Timber.i("<<< UPnP service started successfully")
    }

    override fun getConfiguration(): UpnpServiceConfiguration = configuration

    override fun getControlPoint(): ControlPoint {
        return _controlPoint
    }

    override fun getRegistry(): Registry {
        return _registry
    }

    override fun getRouter(): Router {
        return router
    }

    @Synchronized
    override fun shutdown() {
        this.shutdown(false)
    }

    protected fun shutdown(separateThread: Boolean) {
        val shutdown = Runnable {
            Timber.i(">>> Shutting down UPnP service...")
            shutdownRegistry()
            shutdownRouter()
            shutdownConfiguration()
            Timber.i("<<< UPnP service shutdown completed")
        }
        if (separateThread) {
            Thread(shutdown).start()
        } else {
            shutdown.run()
        }
    }

    private fun shutdownRegistry() {
        registry.shutdown()
    }

    private fun shutdownRouter() {
        try {
            getRouter().shutdown()
        } catch (var3: RouterException) {
            val cause = Exceptions.unwrap(var3)
            if (cause is InterruptedException) {
                Timber.e(cause, "Router shutdown was interrupted: %s", var3.stackTrace)
            } else {
                Timber.e(cause, "Router error on shutdown: $var3")
            }
        }
    }

    private fun shutdownConfiguration() {
        getConfiguration().shutdown()
    }
}
