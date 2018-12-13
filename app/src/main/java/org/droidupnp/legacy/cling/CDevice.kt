package org.droidupnp.legacy.cling

import com.m3sv.plainupnp.data.upnp.UpnpDevice
import org.fourthline.cling.model.meta.Device
import org.fourthline.cling.model.meta.LocalDevice
import org.fourthline.cling.model.types.UDAServiceType
import timber.log.Timber

class CDevice(val device: Device<*, *, *>?) : UpnpDevice {

    override fun getDisplayString(): String = device?.displayString ?: ""

    override fun getFriendlyName(): String = device?.details?.friendlyName ?: displayString

    override fun equals(otherDevice: UpnpDevice): Boolean =
        device?.let {
            it.identity.udn == (otherDevice as CDevice).device?.identity?.udn
        } ?: false

    override fun getUID(): String = device?.identity?.udn?.toString() ?: ""

    override fun getExtendedInformation(): String {
        val info = StringBuilder()
        device?.findServiceTypes()?.forEach {
            info.append("\n\t").append(it.type).append(" : ").append(it.toFriendlyString())
        }
        return info.toString()
    }

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

    override fun getManufacturer(): String =
        device?.details?.manufacturerDetails?.manufacturer ?: ""

    override fun getManufacturerURL(): String =
        device?.details?.manufacturerDetails?.manufacturerURI?.toString() ?: ""

    override fun getModelName(): String = device?.details?.modelDetails?.modelName ?: ""

    override fun getModelDesc(): String = device?.details?.modelDetails?.modelDescription ?: ""

    override fun getModelNumber(): String = device?.details?.modelDetails?.modelNumber ?: ""

    override fun getModelURL(): String = device?.details?.modelDetails?.modelURI?.toString() ?: ""

    override fun getXMLURL(): String = device?.details?.baseURL?.toString() ?: ""

    override fun getPresentationURL(): String = device?.details?.presentationURI?.toString() ?: ""

    override fun getSerialNumber(): String = device?.details?.serialNumber ?: ""

    override fun getUDN(): String = device?.identity?.udn?.toString() ?: ""

    override fun isFullyHydrated(): Boolean = device?.isFullyHydrated ?: false

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

    override fun isLocal(): Boolean = device is LocalDevice
}
