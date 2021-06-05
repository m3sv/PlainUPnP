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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import android.net.wifi.WifiManager.WifiLock
import com.m3sv.plainupnp.upnp.android.NetworkUtils.getConnectedNetworkInfo
import com.m3sv.plainupnp.upnp.android.NetworkUtils.isEthernet
import com.m3sv.plainupnp.upnp.android.NetworkUtils.isMobile
import com.m3sv.plainupnp.upnp.android.NetworkUtils.isWifi
import org.fourthline.cling.UpnpServiceConfiguration
import org.fourthline.cling.model.ModelUtil
import org.fourthline.cling.protocol.ProtocolFactory
import org.fourthline.cling.transport.Router
import org.fourthline.cling.transport.RouterException
import org.fourthline.cling.transport.RouterImpl
import org.seamless.util.Exceptions
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Monitors all network connectivity changes, switching the router accordingly.
 *
 * @author Michael Pujos
 * @author Christian Bauer
 */
class AndroidRouter(
    configuration: UpnpServiceConfiguration?,
    protocolFactory: ProtocolFactory?,
    private val context: Context,
) : RouterImpl(configuration, protocolFactory) {

    private val wifiManager: WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private var multicastLock: MulticastLock? = null
    private var wifiLock: WifiLock? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private var networkInfo: NetworkInfo?

    init {
        networkInfo = getConnectedNetworkInfo(context)

        // Only register for network connectivity changes if we are not running on emulator
        if (!ModelUtil.ANDROID_EMULATOR) {
            broadcastReceiver = ConnectivityBroadcastReceiver()
            context.registerReceiver(broadcastReceiver, IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"))
        }
    }

    override fun getLockTimeoutMillis(): Int = 15000

    @Throws(RouterException::class)
    override fun shutdown() {
        super.shutdown()
        unregisterBroadcastReceiver()
    }

    @Throws(RouterException::class)
    override fun enable(): Boolean {
        lock(writeLock)
        return try {
            var enabled: Boolean
            if (super.enable().also { enabled = it }) {
                // Enable multicast on the WiFi network interface,
                // requires android.permission.CHANGE_WIFI_MULTICAST_STATE
                if (isWifi) {
                    setWiFiMulticastLock(true)
                    setWifiLock(true)
                }
            }
            enabled
        } finally {
            unlock(writeLock)
        }
    }

    @Throws(RouterException::class)
    override fun disable(): Boolean {
        lock(writeLock)
        return try {
            // Disable multicast on WiFi network interface,
            // requires android.permission.CHANGE_WIFI_MULTICAST_STATE
            if (isWifi) {
                setWiFiMulticastLock(false)
                setWifiLock(false)
            }
            super.disable()
        } finally {
            unlock(writeLock)
        }
    }

    private val isMobile: Boolean
        get() = isMobile(networkInfo)
    private val isWifi: Boolean
        get() = isWifi(networkInfo)
    private val isEthernet: Boolean
        get() = isEthernet(networkInfo)

    fun unregisterBroadcastReceiver() {
        if (broadcastReceiver != null) {
            context.unregisterReceiver(broadcastReceiver)
            broadcastReceiver = null
        }
    }

    private fun setWiFiMulticastLock(enable: Boolean) {
        if (multicastLock == null) {
            multicastLock = wifiManager.createMulticastLock(javaClass.simpleName)
        }

        multicastLock?.let { lock ->
            if (enable) {
                if (lock.isHeld) {
                    log.warning("WiFi multicast lock already acquired")
                } else {
                    log.info("WiFi multicast lock acquired")
                    lock.acquire()
                }
            } else {
                if (lock.isHeld) {
                    log.info("WiFi multicast lock released")
                    lock.release()
                } else {
                    log.warning("WiFi multicast lock already released")
                }
            }
        }
    }

    private fun setWifiLock(enable: Boolean) {
        if (wifiLock == null) {
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, javaClass.simpleName)
        }

        wifiLock?.let { lock ->
            if (enable) {
                if (lock.isHeld) {
                    log.warning("WiFi lock already acquired")
                } else {
                    log.info("WiFi lock acquired")
                    lock.acquire()
                }
            } else {
                if (lock.isHeld) {
                    log.info("WiFi lock released")
                    lock.release()
                } else {
                    log.warning("WiFi lock already released")
                }
            }
        }
    }

    /**
     * Can be overriden by subclasses to do additional work.
     *
     * @param oldNetwork `null` when first called by constructor.
     */
    @Throws(RouterException::class)
    private fun onNetworkTypeChange(oldNetwork: NetworkInfo?, newNetwork: NetworkInfo?) {
        log.info(String.format("Network type changed %s => %s",
            if (oldNetwork == null) "" else oldNetwork.typeName,
            if (newNetwork == null) "NONE" else newNetwork.typeName))
        if (disable()) {
            log.info(String.format(
                "Disabled router on network type change (old network: %s)",
                if (oldNetwork == null) "NONE" else oldNetwork.typeName
            ))
        }
        networkInfo = newNetwork
        if (enable()) {
            // Can return false (via earlier InitializationException thrown by NetworkAddressFactory) if
            // no bindable network address found!
            log.info(String.format(
                "Enabled router on network type change (new network: %s)",
                if (newNetwork == null) "NONE" else newNetwork.typeName
            ))
        }
    }

    /**
     * Handles errors when network has been switched, during reception of
     * network switch broadcast. Logs a warning by default, override to
     * change this behavior.
     */
    private fun handleRouterExceptionOnNetworkTypeChange(ex: RouterException) {
        val cause = Exceptions.unwrap(ex)
        if (cause is InterruptedException) {
            log.log(Level.INFO, "Router was interrupted: $ex", cause)
        } else {
            log.log(Level.WARNING, "Router error on network change: $ex", ex)
        }
    }

    internal inner class ConnectivityBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != ConnectivityManager.CONNECTIVITY_ACTION) return
            displayIntentInfo(intent)
            var newNetworkInfo = getConnectedNetworkInfo(context)

            // When Android switches WiFI => MOBILE, sometimes we may have a short transition
            // with no network: WIFI => NONE, NONE => MOBILE
            // The code below attempts to make it look like a single WIFI => MOBILE
            // transition, retrying up to 3 times getting the current network.
            //
            // Note: this can block the UI thread for up to 3s
            networkInfo?.let { oldInfo ->
                if (newNetworkInfo != null) {
                    for (i in 1..3) {
                        try {
                            Thread.sleep(1000)
                        } catch (e: InterruptedException) {
                            return
                        }
                        log.warning(String.format(
                            "%s => NONE network transition, waiting for new network... retry #%d",
                            oldInfo.typeName, i
                        ))
                        newNetworkInfo = getConnectedNetworkInfo(context)
                        if (newNetworkInfo != null) break
                    }
                }
            }

            if (isSameNetworkType(networkInfo, newNetworkInfo)) {
                log.info("No actual network change... ignoring event!")
            } else {
                try {
                    onNetworkTypeChange(networkInfo, newNetworkInfo)
                } catch (ex: RouterException) {
                    handleRouterExceptionOnNetworkTypeChange(ex)
                }
            }
        }

        private fun isSameNetworkType(network1: NetworkInfo?, network2: NetworkInfo?): Boolean {
            if (network1 == null && network2 == null) return true
            return if (network1 == null || network2 == null) false else network1.type == network2.type
        }

        private fun displayIntentInfo(intent: Intent) {
            val noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
            val reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON)
            val isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false)
            val currentNetworkInfo = intent.getParcelableExtra<NetworkInfo>(ConnectivityManager.EXTRA_NETWORK_INFO)
            val otherNetworkInfo = intent.getParcelableExtra<NetworkInfo>(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO)
            log.info("Connectivity change detected...")
            log.info("EXTRA_NO_CONNECTIVITY: $noConnectivity")
            log.info("EXTRA_REASON: $reason")
            log.info("EXTRA_IS_FAILOVER: $isFailover")
            log.info("EXTRA_NETWORK_INFO: " + (currentNetworkInfo ?: "none"))
            log.info("EXTRA_OTHER_NETWORK_INFO: " + (otherNetworkInfo ?: "none"))
            log.info("EXTRA_EXTRA_INFO: " + intent.getStringExtra(ConnectivityManager.EXTRA_EXTRA_INFO))
        }
    }

    companion object {
        private val log = Logger.getLogger(Router::class.java.name)
    }
}
