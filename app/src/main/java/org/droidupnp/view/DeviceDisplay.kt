package org.droidupnp.view

import org.droidupnp.model.upnp.IUpnpDevice


enum class DeviceType {
    RENDERER, CONTENT_DIRECTORY, UNDEFINED
}

data class DeviceDisplay @JvmOverloads constructor(
    val device: IUpnpDevice,
    val extendedInformation: Boolean = false,
    val type: DeviceType = DeviceType.UNDEFINED
)