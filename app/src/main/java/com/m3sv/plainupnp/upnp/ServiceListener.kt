package com.m3sv.plainupnp.upnp

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.IBinder
import android.preference.PreferenceManager
import com.m3sv.plainupnp.common.utils.CONTENT_DIRECTORY_SERVICE
import com.m3sv.plainupnp.common.utils.Utils
import com.m3sv.plainupnp.data.upnp.UpnpDevice
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.droidupnp.legacy.cling.CDevice
import org.droidupnp.legacy.cling.CRegistryListener
import org.droidupnp.legacy.upnp.CallableFilter
import org.droidupnp.legacy.upnp.RegistryListener
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.model.message.header.STAllHeader
import timber.log.Timber
import java.io.IOException
import java.net.UnknownHostException
import java.util.*
import javax.inject.Inject


class ServiceListener @Inject constructor(private val context: Context) {

    var upnpService: AndroidUpnpService? = null
        private set

    private val waitingListener: MutableList<RegistryListener> = mutableListOf()

    private var mediaServer: MediaServer? = null

    val deviceList: Collection<UpnpDevice>
        get() {
            val deviceList = mutableListOf<UpnpDevice>()
            upnpService?.registry?.let {
                deviceList.addAll(it.devices.map { device -> CDevice(device) })
            }
            return deviceList
        }

    // Local content directory
    // Search asynchronously for all devices, they will respond soon
    val serviceConnection: ServiceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            GlobalScope.launch {
                Timber.i("Connected service")
                upnpService = service as AndroidUpnpService

                val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
                if (sharedPref.getBoolean(CONTENT_DIRECTORY_SERVICE, true)) {
                    try {
                        mediaServer = mediaServer
                                ?: MediaServer(context, Utils.getLocalIpAddress(context)).apply { start() }

                        upnpService?.registry?.addDevice(mediaServer?.localDevice)
                    } catch (e1: UnknownHostException) {
                        Timber.e(e1, "Creating demo device failed")
                    } catch (e3: IOException) {
                        Timber.e(e3, "Starting http server failed")
                    }

                } else if (mediaServer != null) {
                    mediaServer?.stop()
                    mediaServer = null
                }

                waitingListener.map { addListenerSafe(it) }

                upnpService?.controlPoint?.search()
            }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Timber.i("Service disconnected")
            upnpService = null
        }
    }

    fun refresh() {
        upnpService?.controlPoint?.search(STAllHeader())
    }

    fun getFilteredDeviceList(filter: CallableFilter): Collection<UpnpDevice> {
        val deviceList = ArrayList<UpnpDevice>()
        try {
            upnpService?.registry?.devices?.forEach {
                val device = CDevice(it)
                filter.device = device

                if (filter.call())
                    deviceList.add(device)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        return deviceList
    }

    fun addListener(registryListener: RegistryListener) {
        if (upnpService != null)
            addListenerSafe(registryListener)
        else
            waitingListener.add(registryListener)
    }

    private fun addListenerSafe(registryListener: RegistryListener) {
        assert(upnpService != null)

        upnpService?.registry?.run {
            // Get ready for future device advertisements
            addListener(CRegistryListener(registryListener))

            // Now add all devices to the list we already know about
            devices?.forEach {
                registryListener.deviceAdded(CDevice(it))
            }
        }
    }

    fun removeListener(registryListener: RegistryListener) {
        if (upnpService != null)
            removeListenerSafe(registryListener)
        else
            waitingListener.remove(registryListener)
    }

    private fun removeListenerSafe(registryListener: RegistryListener) {
        assert(upnpService != null)
        upnpService?.registry?.removeListener(CRegistryListener(registryListener))
    }

    fun clearListener() {
        waitingListener.clear()
    }
}
