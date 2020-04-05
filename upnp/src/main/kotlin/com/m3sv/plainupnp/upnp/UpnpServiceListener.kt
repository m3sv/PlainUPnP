package com.m3sv.plainupnp.upnp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.upnp.cling.AndroidUpnpService
import com.m3sv.plainupnp.upnp.filters.CallableFilter
import com.m3sv.plainupnp.upnp.resourceproviders.LocalServiceResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.message.header.STAllHeader
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpnpServiceListener @Inject constructor(private val context: Context) {

    var upnpService: AndroidUpnpService? = null
        private set

    val controlPoint: ControlPoint?
        get() = upnpService?.controlPoint

    private val waitingListener: MutableList<RegistryListener> = mutableListOf()

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            // no-op
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            GlobalScope.launch(Dispatchers.IO) {
                upnpService = (service as AndroidUpnpService).also { upnpService ->
                    upnpService.controlPoint.search()
                    val localUpnpDevice = LocalUpnpDevice.getLocalDevice(
                        LocalServiceResourceProvider(context),
                        context
                    )
                    upnpService.registry.addDevice(localUpnpDevice)
                }
                waitingListener.map { addListenerSafe(it) }
            }
        }
    }

    fun bindService() {
        context.bindService(
            Intent(context, PlainUpnpAndroidService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun unbindService() {
        Timber.d("Unbind!")
        upnpService = null
        context.unbindService(serviceConnection)
    }

    fun getFilteredDeviceList(filter: CallableFilter): Collection<UpnpDevice> {
        val deviceList = mutableListOf<UpnpDevice>()

        try {
            upnpService?.registry?.devices?.forEach {
                val device = CDevice(it)
                filter.device = device

                if (filter.call()) deviceList.add(device)
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
        upnpService?.registry?.removeListener(CRegistryListener(registryListener))
    }

    fun clearListener() {
        waitingListener.clear()
    }

    fun refresh() {
        upnpService?.controlPoint?.search(STAllHeader())
    }
}
