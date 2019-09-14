package com.m3sv.plainupnp.upnp.usecase

import com.m3sv.plainupnp.upnp.UpnpStateStore
import javax.inject.Inject

class ObserveUpnpStateUseCase @Inject constructor(
    private val upnpStateStore: UpnpStateStore
) {
    fun execute() = upnpStateStore.state
}