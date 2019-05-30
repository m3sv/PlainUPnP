package com.m3sv.plainupnp.upnp

import android.content.Context
import android.net.wifi.WifiManager
import android.preference.PreferenceManager
import timber.log.Timber
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException


const val CONTENT_DIRECTORY_SERVICE = "pref_contentDirectoryService"
const val CONTENT_DIRECTORY_VIDEO = "pref_contentDirectoryService_video"
const val CONTENT_DIRECTORY_AUDIO = "pref_contentDirectoryService_audio"
const val CONTENT_DIRECTORY_IMAGE = "pref_contentDirectoryService_image"
const val CONTENT_DIRECTORY_NAME = "pref_contentDirectoryService_name"

const val PORT = 8192

fun getSettingContentDirectoryName(context: Context): String =
        PreferenceManager.getDefaultSharedPreferences(context)
                .getString(CONTENT_DIRECTORY_NAME, android.os.Build.MODEL) ?: android.os.Build.MODEL

private fun getLocalIpAddressFromIntf(intfName: String): InetAddress? {
    try {
        val intf = NetworkInterface.getByName(intfName)
        if (intf.isUp) {
            val enumIpAddr = intf.inetAddresses
            while (enumIpAddr.hasMoreElements()) {
                val inetAddress = enumIpAddr.nextElement()
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address)
                    return inetAddress
            }
        }
    } catch (e: Exception) {
        Timber.d("Unable to get ip adress for interface $intfName")
    }

    return null
}

@Throws(UnknownHostException::class)
fun getLocalIpAddress(context: Context): InetAddress {
    println("Context: $context")
    val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    wifiManager?.let {
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress
        if (ipAddress != 0)
            return InetAddress.getByName(
                    String.format(
                            "%d.%d.%d.%d",
                            ipAddress and 0xff, ipAddress shr 8 and 0xff,
                            ipAddress shr 16 and 0xff, ipAddress shr 24 and 0xff
                    )
            )

        Timber.d("No ip adress available throught wifi manager, try to get it manually")

        var inetAddress: InetAddress? =
                getLocalIpAddressFromIntf("wlan0")

        if (inetAddress != null) {
            Timber.d("Got an ip for interfarce wlan0")
            return inetAddress
        }

        inetAddress = getLocalIpAddressFromIntf("usb0")
        if (inetAddress != null) {
            Timber.d("Got an ip for interface usb0")
            return inetAddress
        }
    }

    return InetAddress.getByName("0.0.0.0")
}