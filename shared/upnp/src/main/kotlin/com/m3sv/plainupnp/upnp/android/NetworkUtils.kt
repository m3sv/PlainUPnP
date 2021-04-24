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

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import org.fourthline.cling.model.ModelUtil
import java.util.logging.Logger

/**
 * Android network helpers.
 *
 * @author Michael Pujos
 */
internal object NetworkUtils {

    private val log = Logger.getLogger(NetworkUtils::class.java.name)

    @JvmStatic
    fun getConnectedNetworkInfo(context: Context): NetworkInfo? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected) return networkInfo

        networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected) return networkInfo

        networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
        if (networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected) return networkInfo

        networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX)
        if (networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected) return networkInfo

        networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET)
        if (networkInfo != null && networkInfo.isAvailable && networkInfo.isConnected) return networkInfo

        log.info("Could not find any connected network...")

        return null
    }

    @JvmStatic
    fun isEthernet(networkInfo: NetworkInfo?): Boolean = isNetworkType(networkInfo, ConnectivityManager.TYPE_ETHERNET)

    @JvmStatic
    fun isWifi(networkInfo: NetworkInfo?): Boolean =
        isNetworkType(networkInfo, ConnectivityManager.TYPE_WIFI) || ModelUtil.ANDROID_EMULATOR

    @JvmStatic
    fun isMobile(networkInfo: NetworkInfo?): Boolean = isNetworkType(
        networkInfo,
        ConnectivityManager.TYPE_MOBILE) || isNetworkType(networkInfo,
        ConnectivityManager.TYPE_WIMAX
    )

    private fun isNetworkType(networkInfo: NetworkInfo?, type: Int): Boolean =
        networkInfo != null && networkInfo.type == type

    private fun isSSDPAwareNetwork(networkInfo: NetworkInfo?): Boolean = isWifi(networkInfo) || isEthernet(networkInfo)
}
