package com.m3sv.plainupnp.data.upnp

interface UpnpDevice {
    val displayString: String
    val friendlyName: String
    val isFullyHydrated: Boolean
    fun asService(service: String): Boolean
    fun printService()
}
