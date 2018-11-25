package com.m3sv.plainupnp.common

import android.content.Context
import android.net.wifi.WifiManager
import android.provider.MediaStore
import timber.log.Timber
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.UnknownHostException
import java.util.*

/** General utils that are available throughout the application */
object Utils {
    @JvmStatic
    fun getAllMedia(context: Context): ArrayList<String> {
        val videoItemHashSet = HashSet<String>()
        val projection =
            arrayOf(MediaStore.Video.VideoColumns.DATA, MediaStore.Video.Media.DISPLAY_NAME)
        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        try {
            cursor.moveToFirst()
            do {
                videoItemHashSet.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)))
            } while (cursor.moveToNext())

            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ArrayList(videoItemHashSet)
    }

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
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
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

        var inetAddress: InetAddress? = getLocalIpAddressFromIntf("wlan0")

        if (inetAddress != null) {
            Timber.d("Got an ip for interfarce wlan0")
            return inetAddress
        }

        inetAddress = getLocalIpAddressFromIntf("usb0")
        if (inetAddress != null) {
            Timber.d("Got an ip for interface usb0")
            return inetAddress
        }

        return InetAddress.getByName("0.0.0.0")
    }
}