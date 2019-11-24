package com.m3sv.plainupnp.data.upnp

enum class DeviceType {
    RENDERER,
    CONTENT_DIRECTORY,
    PLAY_LOCALLY,
    UNDEFINED
}

data class DeviceDisplay(
    val device: UpnpDevice,
    val extendedInformation: Boolean = false,
    val type: DeviceType = DeviceType.UNDEFINED
)
