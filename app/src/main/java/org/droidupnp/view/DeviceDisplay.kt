package org.droidupnp.view

import org.droidupnp.model.upnp.IUPnPDevice


data class DeviceDisplay @JvmOverloads constructor(val device: IUPnPDevice, val extendedInformation: Boolean = false)