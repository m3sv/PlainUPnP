package com.m3sv.plainupnp.upnp.usecase

import com.m3sv.plainupnp.upnp.ContentState
import com.m3sv.plainupnp.upnp.UpnpStateStore
import kotlinx.coroutines.channels.ReceiveChannel
import javax.inject.Inject

class ObserveUpnpStateUseCase @Inject constructor(private val upnpStateStore: UpnpStateStore) {
    fun execute(): ReceiveChannel<ContentState> = upnpStateStore.state
}