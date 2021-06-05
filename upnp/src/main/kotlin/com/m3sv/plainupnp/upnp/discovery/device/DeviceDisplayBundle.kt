package com.m3sv.plainupnp.upnp.discovery.device

import com.m3sv.plainupnp.data.upnp.DeviceDisplay

data class DeviceDisplayBundle(
    val devices: List<DeviceDisplay>,
    val selectedDeviceText: String?
)
