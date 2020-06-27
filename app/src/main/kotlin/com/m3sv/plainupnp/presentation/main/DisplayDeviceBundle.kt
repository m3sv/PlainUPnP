package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.presentation.base.SpinnerItem

data class SpinnerItemsBundle(
    val devices: List<SpinnerItem>,
    val selectedDeviceIndex: Int,
    val selectedDeviceName: String?
)
