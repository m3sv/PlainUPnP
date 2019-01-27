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

package com.m3sv.plainupnp.upnp.discovery


import com.m3sv.plainupnp.data.upnp.UpnpDevice
import com.m3sv.plainupnp.data.upnp.UpnpDeviceEvent
import com.m3sv.plainupnp.upnp.ServiceListener
import com.m3sv.plainupnp.upnp.UpnpServiceController
import org.droidupnp.legacy.upnp.CallableFilter
import org.droidupnp.legacy.upnp.DeviceDiscoveryObserver
import org.droidupnp.legacy.upnp.RegistryListener
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList

abstract class DeviceDiscovery(protected val controller: UpnpServiceController) {

    protected abstract val callableFilter: CallableFilter

    private val browsingRegistryListener: BrowsingRegistryListener = BrowsingRegistryListener()

    private val observerList: CopyOnWriteArrayList<DeviceDiscoveryObserver> = CopyOnWriteArrayList()

    fun resume(serviceListener: ServiceListener) {
        serviceListener.addListener(browsingRegistryListener)
    }

    fun pause(serviceListener: ServiceListener) {
        with(serviceListener) {
            removeListener(browsingRegistryListener)
            clearListener()
        }
    }

    inner class BrowsingRegistryListener : RegistryListener {

        override fun deviceAdded(device: UpnpDevice) {
            Timber.v("New device detected : " + device.displayString)

            if (device.isFullyHydrated && filter(device)) {
                if (isSelected(device)) {
                    Timber.i("Reselect device to refresh it")
                    select(device, true)
                }

                notifyAdded(device)
            }
        }

        override fun deviceRemoved(device: UpnpDevice) {
            Timber.v("Device removed : " + device.friendlyName)

            if (filter(device)) {
                if (isSelected(device)) {
                    Timber.i("Selected device have been removed")
                    removed(device)
                }

                notifyRemoved(device)
            }
        }
    }

    fun hasObserver(o: DeviceDiscoveryObserver): Boolean {
        return observerList.contains(o)
    }

    fun addObserver(o: DeviceDiscoveryObserver) {
        observerList.add(o)

        controller
            .serviceListener
            .getFilteredDeviceList(callableFilter)
            .forEach { o.addedDevice(UpnpDeviceEvent.Added(it)) }
    }

    fun removeObserver(o: DeviceDiscoveryObserver) {
        observerList.remove(o)
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

    protected abstract fun isSelected(device: UpnpDevice): Boolean

    protected abstract fun select(device: UpnpDevice)

    protected abstract fun select(device: UpnpDevice, force: Boolean)

    protected abstract fun removed(device: UpnpDevice)
}
