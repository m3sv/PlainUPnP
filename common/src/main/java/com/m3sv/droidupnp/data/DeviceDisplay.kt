package com.m3sv.droidupnp.data


enum class DeviceType {
    RENDERER, CONTENT_DIRECTORY, UNDEFINED
}

data class DeviceDisplay @JvmOverloads constructor(
    val device: IUpnpDevice,
    val extendedInformation: Boolean = false,
    val type: DeviceType = DeviceType.UNDEFINED
)