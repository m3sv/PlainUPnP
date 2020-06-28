package com.m3sv.plainupnp.data.upnp

interface UpnpDevice {
    val identity: String
    val displayString: String
    val friendlyName: String
    val fullIdentity: String
    val isFullyHydrated: Boolean
    val isLocal: Boolean
    fun asService(service: String): Boolean
    fun printService()
}
