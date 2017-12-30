package org.droidupnp.view

import org.droidupnp.model.upnp.IUPnPDevice


enum class DeviceType {
    RENDERER, CONTENT_DIRECTORY, UNDEFINED
}

data class DeviceDisplay @JvmOverloads constructor(val device: IUPnPDevice,  val extendedInformation: Boolean = false, val type: DeviceType = DeviceType.UNDEFINED)