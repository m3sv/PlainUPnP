package com.m3sv.plainupnp.upnp.util

import android.content.Context
import android.net.wifi.WifiManager
import timber.log.Timber
import java.net.*

const val CONTENT_DIRECTORY_SERVICE = "pref_contentDirectoryService"
const val CONTENT_DIRECTORY_VIDEO = "pref_contentDirectoryService_video"
const val CONTENT_DIRECTORY_AUDIO = "pref_contentDirectoryService_audio"
const val CONTENT_DIRECTORY_IMAGE = "pref_contentDirectoryService_image"
const val CONTENT_DIRECTORY_NAME = "pref_contentDirectoryService_name"

const val PORT = 8192

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
        Timber.d("Unable to get ip address for interface $intfName")
    }

    return null
}

@Throws(UnknownHostException::class)
fun getLocalIpAddress(context: Context): InetAddress {
    val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    if (wifiManager != null) {
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress
        if (ipAddress != 0) {
            try {
                return InetAddress.getByName(
                    String.format(
                        "%d.%d.%d.%d",
                        ipAddress and 0xff, ipAddress shr 8 and 0xff,
                        ipAddress shr 16 and 0xff, ipAddress shr 24 and 0xff
                    )
                )

            } catch (e: Exception) {
                Timber.e("Could not retrieve InetAddress by name")
            }
        }

        Timber.d("No ip address available through wifi manager, try to get it manually")

        var inetAddress: InetAddress? =
            getLocalIpAddressFromIntf("wlan0")

        if (inetAddress != null) {
            Timber.d("Got an ip for interfarce wlan0")
            return inetAddress
        }

        inetAddress =
            getLocalIpAddressFromIntf("usb0")
        if (inetAddress != null) {
            Timber.d("Got an ip for interface usb0")
            return inetAddress
        }
    }

    return InetSocketAddress(0).address
}
