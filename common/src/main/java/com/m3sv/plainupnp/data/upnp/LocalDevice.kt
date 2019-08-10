package com.m3sv.plainupnp.data.upnp

//private val _friendlyName: String = "Play locally"
class LocalDevice(override val displayString: String = "",
                  override val friendlyName: String = "Play locally",
                  override val extendedInformation: String = "",
                  override val manufacturer: String = "",
                  override val manufacturerUrl: String = "",
                  override val modelName: String = "",
                  override val modelDesc: String = "",
                  override val modelNumber: String = "",
                  override val modelUrl: String = "",
                  override val xmlUrl: String = "",
                  override val presentationURL: String = "",
                  override val serialNumber: String = "",
                  override val udn: String = "",
                  override val uid: String = "",
                  override val isFullyHydrated: Boolean = false,
                  override val isLocal: Boolean = true) : UpnpDevice {

    override fun equals(otherDevice: UpnpDevice?): Boolean = otherDevice is LocalDevice

    override fun asService(service: String): Boolean = false

    override fun printService() {
        // do nothing
    }
}

