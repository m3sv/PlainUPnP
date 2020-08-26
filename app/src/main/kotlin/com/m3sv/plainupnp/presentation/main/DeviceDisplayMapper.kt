package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.presentation.base.SpinnerItem
import com.m3sv.plainupnp.upnp.discovery.device.DeviceDisplayBundle
import javax.inject.Inject

class DeviceDisplayMapper @Inject constructor() : (DeviceDisplayBundle) -> SpinnerItemsBundle {

    override fun invoke(input: DeviceDisplayBundle): SpinnerItemsBundle {
        val items = input.devices.map { SpinnerItem(it.device.friendlyName) }
        return SpinnerItemsBundle(
            items,
            input.selectedDeviceIndex,
            input.selectedDeviceText
        )
    }

}
