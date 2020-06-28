package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.common.Mapper
import com.m3sv.plainupnp.presentation.base.SpinnerItem
import com.m3sv.plainupnp.upnp.discovery.device.DeviceDisplayBundle
import javax.inject.Inject

class DeviceDisplayMapper @Inject constructor() : Mapper<DeviceDisplayBundle, SpinnerItemsBundle> {

    override fun map(input: DeviceDisplayBundle): SpinnerItemsBundle {
        val items = input.devices.map { SpinnerItem(it.device.friendlyName) }
        return SpinnerItemsBundle(
            items,
            input.selectedDeviceIndex,
            input.selectedDeviceText
        )
    }
}
