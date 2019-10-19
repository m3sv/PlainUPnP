package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.types.UDAServiceType
import timber.log.Timber

class CDevice(val device: Device<*, *, *>?) : UpnpDevice {

    override val displayString: String = device?.displayString ?: ""

    override val friendlyName: String = device?.details?.friendlyName ?: displayString

    override val manufacturer: String = device?.details?.manufacturerDetails?.manufacturer ?: ""

    override val manufacturerUrl: String = device?.details?.manufacturerDetails?.manufacturerURI?.toString()
            ?: ""

    override val modelName: String = device?.details?.modelDetails?.modelName ?: ""

    override val modelDesc: String = device?.details?.modelDetails?.modelDescription ?: ""

    override val modelNumber: String = device?.details?.modelDetails?.modelNumber ?: ""

    override val modelUrl: String = device?.details?.modelDetails?.modelURI?.toString() ?: ""

    override val xmlUrl: String = device?.details?.baseURL?.toString() ?: ""

    override val presentationURL: String = device?.details?.presentationURI?.toString() ?: ""

    override val serialNumber: String = device?.details?.serialNumber ?: ""

    override val udn: String = device?.identity?.udn?.toString() ?: ""

    override val uid: String = device?.identity?.udn?.toString() ?: ""

    override val isFullyHydrated: Boolean = device?.isFullyHydrated ?: false

    override val isLocal: Boolean = device is LocalDevice

    override val extendedInformation: String
        get() {
            val info = StringBuilder()
            device?.findServiceTypes()?.forEach {
                info.append("\n\t").append(it.type).append(" : ").append(it.toFriendlyString())
            }
            return info.toString()
        }

    override fun equals(otherDevice: UpnpDevice?): Boolean =
            device?.let {
                it.identity.udn == (otherDevice as CDevice).device?.identity?.udn
            } ?: false


    override fun printService() {
        device?.findServices()?.forEach {
            Timber.i("\t Service : $it")
            for (a in it.actions) {
                Timber.i("\t\t Action : $a")
            }
        }
    }

    override fun asService(service: String): Boolean =
            device?.findService(UDAServiceType(service)) != null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val cDevice = other as? CDevice

        return device?.let {
            it == cDevice?.device
        } ?: false
    }

    override fun hashCode(): Int = device?.hashCode() ?: 0

    override fun toString(): String = "CDevice{ device=$device }"

}
