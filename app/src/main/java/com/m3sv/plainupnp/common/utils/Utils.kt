package com.m3sv.plainupnp.common.utils

import android.content.Context
import android.net.wifi.WifiManager
import timber.log.Timber
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException

/** General utils that are available throughout the application */
object Utils {
    @JvmStatic
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

    @JvmStatic
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
}