/**
 * Copyright (C) 2013 Aurélien Chabot <aurelien></aurelien>@chabot.fr>
 *
 *
 * This file is part of DroidUPNP.
 *
 *
 * DroidUPNP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *
 * DroidUPNP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with DroidUPNP.  If not, see <http:></http:>//www.gnu.org/licenses/>.
 */

package com.m3sv.plainupnp.upnp.discovery.device


import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.data.upnp.UpnpDeviceEvent
import com.m3sv.plainupnp.upnp.CDevice
import com.m3sv.plainupnp.upnp.CRegistryListener
import com.m3sv.plainupnp.upnp.RegistryListener
import com.m3sv.plainupnp.upnp.filters.CallableFilter
import org.fourthline.cling.UpnpService
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList

abstract class DeviceDiscovery(val upnpService: UpnpService) {

    private val browsingRegistryListener: BrowsingRegistryListener = BrowsingRegistryListener()

    protected abstract val callableFilter: CallableFilter

    private val observerList: CopyOnWriteArrayList<DeviceDiscoveryObserver> = CopyOnWriteArrayList()

    inner class BrowsingRegistryListener : RegistryListener {

        override fun deviceAdded(device: UpnpDevice) {
            Timber.v("Device added: %s", device.displayString)

            if (device.isFullyHydrated && filter(device)) {
                notifyAdded(device)
            }
        }

        override fun deviceRemoved(device: UpnpDevice) {
            Timber.v("Device removed: %s", device.friendlyName)

            if (filter(device)) {
                notifyRemoved(device)
            }
        }
    }

    fun addObserver(o: DeviceDiscoveryObserver) {
        observerList.add(o)

        upnpService
            .getFilteredDeviceList(callableFilter)
            .forEach { o.addedDevice(UpnpDeviceEvent.Added(it)) }
    }

    fun removeObserver(o: DeviceDiscoveryObserver) {
        observerList.remove(o)
    }

    private fun UpnpService.getFilteredDeviceList(filter: CallableFilter): Collection<UpnpDevice> {
        val deviceList = mutableListOf<UpnpDevice>()

        try {
            registry?.devices?.forEach {
                val device = CDevice(it)
                filter.device = device

                if (filter.call()) deviceList.add(device)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        return deviceList
    }

    private fun notifyAdded(device: UpnpDevice) {
        for (o in observerList)
            o.addedDevice(UpnpDeviceEvent.Added(device))
    }

    private fun notifyRemoved(device: UpnpDevice) {
        for (o in observerList)
            o.removedDevice(UpnpDeviceEvent.Removed(device))
    }

    /**
     * Filter device you want to add to this device list fragment
     *
     * @param device the device to test
     * @return add it or not
     */
    protected fun filter(device: UpnpDevice): Boolean {
        callableFilter.device = device
        try {
            return callableFilter.call()
        } catch (e: Exception) {
            Timber.e(e)
        }

        return false
    }

    fun startObserving() {
        upnpService.addListenerSafe(browsingRegistryListener)
    }

    fun stopObserving() {
        upnpService.removeListenerSafe(browsingRegistryListener)
    }

    fun UpnpService.addListenerSafe(registryListener: RegistryListener) {
        registry?.run {
            // Get ready for future device advertisements
            addListener(CRegistryListener(registryListener))

            // Now add all devices to the list we already know about
            devices?.forEach {
                registryListener.deviceAdded(CDevice(it))
            }
        }
    }

    fun UpnpService.removeListenerSafe(registryListener: RegistryListener) {
        registry?.removeListener(CRegistryListener(registryListener))
    }
}
