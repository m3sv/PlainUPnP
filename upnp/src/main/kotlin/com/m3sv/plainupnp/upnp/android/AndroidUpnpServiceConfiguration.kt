/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.m3sv.plainupnp.upnp.android

import android.os.Build
import okhttp3.OkHttpClient
import org.fourthline.cling.DefaultUpnpServiceConfiguration
import org.fourthline.cling.binding.xml.DeviceDescriptorBinder
import org.fourthline.cling.binding.xml.RecoveringUDA10DeviceDescriptorBinderImpl
import org.fourthline.cling.binding.xml.ServiceDescriptorBinder
import org.fourthline.cling.binding.xml.UDA10ServiceDescriptorBinderSAXImpl
import org.fourthline.cling.model.Namespace
import org.fourthline.cling.model.ServerClientTokens
import org.fourthline.cling.transport.impl.AsyncServletStreamServerConfigurationImpl
import org.fourthline.cling.transport.impl.AsyncServletStreamServerImpl
import org.fourthline.cling.transport.impl.RecoveringGENAEventProcessorImpl
import org.fourthline.cling.transport.impl.RecoveringSOAPActionProcessorImpl
import org.fourthline.cling.transport.impl.jetty.JettyServletContainer
import org.fourthline.cling.transport.impl.jetty.OkHttpStreamClient
import org.fourthline.cling.transport.impl.jetty.StreamClientConfigurationImpl
import org.fourthline.cling.transport.spi.*
import java.util.concurrent.TimeUnit

/**
 * Configuration settings for deployment on Android.
 * <p>
 * This configuration utilizes the Jetty transport implementation
 * found in {@link org.fourthline.cling.transport.impl.jetty} for TCP/HTTP networking, as
 * client and server. The servlet context path for UPnP is set to <code>/upnp</code>.
 * </p>
 * <p>
 * The kxml2 implementation of <code>org.xmlpull</code> is available on Android, therefore
 * this configuration uses {@link RecoveringUDA10DeviceDescriptorBinderImpl},
 * {@link RecoveringSOAPActionProcessorImpl}, and {@link RecoveringGENAEventProcessorImpl}.
 * </p>
 * <p>
 * This configuration utilizes {@link UDA10ServiceDescriptorBinderSAXImpl}, the system property
 * <code>org.xml.sax.driver</code> is set to  <code>org.xmlpull.v1.sax2.Driver</code>.
 * </p>
 * <p>
 * To preserve battery, the {@link Registry} will only
 * be maintained every 3 seconds.
 * </p>
 *
 * @author Christian Bauer
 */
abstract class AndroidUpnpServiceConfiguration : DefaultUpnpServiceConfiguration {

    private val servletContainer: JettyServletContainer = JettyServletContainer.INSTANCE

    constructor() : this(0)

    constructor(streamListenPort: Int) : super(streamListenPort, false)

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(3, TimeUnit.MINUTES)
        .writeTimeout(3, TimeUnit.MINUTES)
        .build()

    private val streamClientConfiguration = object : StreamClientConfigurationImpl(syncProtocolExecutorService) {
        override fun getUserAgentValue(majorVersion: Int, minorVersion: Int): String {
            // TODO: UPNP VIOLATION: Synology NAS requires User-Agent to contain
            // "Android" to return DLNA protocolInfo required to stream to Samsung TV
            // see: http://two-play.com/forums/viewtopic.php?f=6&t=81
            val tokens = ServerClientTokens(majorVersion, minorVersion)
            tokens.osName = "Android"
            tokens.osVersion = Build.VERSION.RELEASE
            return tokens.toString()
        }
    }

    override fun createNamespace(): Namespace = // For the Jetty server, this is the servlet context path
        Namespace("/upnp")

    override fun createStreamClient(): StreamClient<*> = OkHttpStreamClient(
        okHttpClient,
        streamClientConfiguration
    )

    override fun createStreamServer(networkAddressFactory: NetworkAddressFactory): StreamServer<*> =
        AsyncServletStreamServerImpl(
            AsyncServletStreamServerConfigurationImpl(
                servletContainer,
                networkAddressFactory.streamListenPort
            )
        )

    override fun createDeviceDescriptorBinderUDA10(): DeviceDescriptorBinder =
        RecoveringUDA10DeviceDescriptorBinderImpl()

    override fun createServiceDescriptorBinderUDA10(): ServiceDescriptorBinder =
        UDA10ServiceDescriptorBinderSAXImpl(DlnaSaxParser())

    override fun createSOAPActionProcessor(): SOAPActionProcessor = RecoveringSOAPActionProcessorImpl()

    override fun createGENAEventProcessor(): GENAEventProcessor = RecoveringGENAEventProcessorImpl()

    override fun getRegistryMaintenanceIntervalMillis(): Int =
        3000 // Preserve battery on Android, only run every 3 seconds
}
