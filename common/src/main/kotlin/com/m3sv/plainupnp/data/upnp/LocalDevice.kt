package com.m3sv.plainupnp.data.upnp

const val PLAINUPNP_LOCAL_DEVICE = "plainupnp-localdevice"

class LocalDevice(
    override val displayString: String = "PlainUPnP local device",
    override val friendlyName: String = "Play locally",
    override val isFullyHydrated: Boolean = false
) : UpnpDevice {

    override fun asService(service: String): Boolean = true

    override fun printService() {
        // do nothing
    }

    override val identity: String = PLAINUPNP_LOCAL_DEVICE
    override val isLocal: Boolean = true
    override val fullIdentity: String = "${identity}:${displayString}:${friendlyName}"
}

