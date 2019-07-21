package com.m3sv.plainupnp.upnp.cleanslate

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.fourthline.cling.android.AndroidUpnpService
import org.fourthline.cling.model.message.header.STAllHeader
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class UpnpServiceListener @Inject constructor(private val context: Context) : CoroutineScope {

    var upnpService: AndroidUpnpService? = null
        private set

    private val job = Job()

    override val coroutineContext: CoroutineContext = Dispatchers.IO + job

    // getLocalIpAddress(context)
    private var mediaServer: MediaServer = MediaServer(context)

    private val waitingListener: MutableList<RegistryListener> = mutableListOf()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mediaServer.start()
            upnpService = (service as AndroidUpnpService).also {
                it.controlPoint.search()
                it.registry.addDevice(LocalUpnpDevice(LocalServiceResourceProvider(context), LocalService(context, getLocalIpAddress(context)))())
            }
            waitingListener.map { addListenerSafe(it) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            upnpService = null
            mediaServer.stop()
        }
    }

    fun bindService() {
        context.bindService(Intent(
                context,
                com.m3sv.plainupnp.upnp.AndroidUpnpService::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        )
    }

    fun unbindService() {
        context.unbindService(serviceConnection)
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

    fun refresh() {
        upnpService?.controlPoint?.search(STAllHeader())
    }
}