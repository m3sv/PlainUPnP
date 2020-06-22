package com.m3sv.plainupnp.data.upnp

class LocalDevice(
    override val displayString: String = "",
    override val friendlyName: String = "Play locally",
    override val isFullyHydrated: Boolean = false
) : UpnpDevice {

    override fun asService(service: String): Boolean = true

    override fun printService() {
        // do nothing
    }
}

