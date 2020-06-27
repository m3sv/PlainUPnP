package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.types.UDAServiceType
import timber.log.Timber

data class CDevice(val device: Device<*, *, *>) : UpnpDevice {

    private val details = device.details

    override val displayString: String = device.displayString

    override val friendlyName: String = details?.friendlyName ?: displayString

    override val isFullyHydrated: Boolean = device.isFullyHydrated

    override fun printService() {
        device.findServices()?.forEach { service ->
            Timber.i("\t Service : $service")
            for (a in service.actions) {
                Timber.i("\t\t Action : $a")
            }
        }
    }

    override fun asService(service: String): Boolean =
        device.findService(UDAServiceType(service)) != null

    override val identity: String = device.identity.udn.identifierString
    override val fullIdentity: String = "${identity}:${displayString}:${friendlyName}"
    override val isLocal: Boolean = device is LocalDevice
}
