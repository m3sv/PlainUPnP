package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.common.Mapper
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.presentation.base.SpinnerItem
import javax.inject.Inject

class DeviceDisplayMapper @Inject constructor() : Mapper<List<DeviceDisplay>, List<SpinnerItem>> {

    override fun map(input: List<DeviceDisplay>): List<SpinnerItem> =
        input.map { SpinnerItem(it.device.friendlyName) }

}
