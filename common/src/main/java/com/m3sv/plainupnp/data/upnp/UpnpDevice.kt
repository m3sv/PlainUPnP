package com.m3sv.plainupnp.data.upnp

interface UpnpDevice {

    val displayString: String

    val friendlyName: String

    val extendedInformation: String

    val manufacturer: String

    val manufacturerURL: String

    val modelName: String

    val modelDesc: String

    val modelNumber: String

    val modelURL: String

    val xmlurl: String

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
