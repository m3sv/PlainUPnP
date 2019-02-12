package com.m3sv.plainupnp.data.upnp

interface UpnpDevice {

    val displayString: String

    val friendlyName: String

    val extendedInformation: String

    val manufacturer: String

    val manufacturerUrl: String

    val modelName: String

    val modelDesc: String

    val modelNumber: String

    val modelUrl: String

    val xmlUrl: String

    val presentationURL: String

    val serialNumber: String

    val udn: String

    val uid: String

    val isFullyHydrated: Boolean

    val isLocal: Boolean

    fun equals(otherDevice: UpnpDevice?): Boolean

    fun asService(service: String): Boolean

    fun printService()
}
