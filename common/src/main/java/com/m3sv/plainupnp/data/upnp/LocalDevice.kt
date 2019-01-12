package com.m3sv.plainupnp.data.upnp


class LocalDevice(private val _friendlyName: String = "Play locally") : UpnpDevice {
    override fun getDisplayString(): String = ""

    override fun getFriendlyName(): String = _friendlyName

    override fun getExtendedInformation(): String = ""

    override fun getManufacturer(): String = ""

    override fun getManufacturerURL(): String = ""

    override fun getModelName(): String = ""

    override fun getModelDesc(): String = ""

    override fun getModelNumber(): String = ""

    override fun getModelURL(): String = ""

    override fun getXMLURL(): String = ""

    override fun getPresentationURL(): String = ""

    override fun getSerialNumber(): String = ""

    override fun getUDN(): String = ""

    override fun equals(otherDevice: UpnpDevice?): Boolean = otherDevice is LocalDevice

    override fun getUID(): String = ""

    override fun asService(service: String?): Boolean = false

    override fun printService() {
        // do nothing
    }

    override fun isFullyHydrated(): Boolean = false

    override fun isLocal(): Boolean = true
}

