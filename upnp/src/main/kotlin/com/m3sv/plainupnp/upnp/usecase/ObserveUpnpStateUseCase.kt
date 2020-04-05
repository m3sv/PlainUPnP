package com.m3sv.plainupnp.upnp.usecase

import com.m3sv.plainupnp.upnp.ContentState
import com.m3sv.plainupnp.upnp.UpnpStateStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUpnpStateUseCase @Inject constructor(private val upnpStateStore: UpnpStateStore) {
    fun execute(): Flow<ContentState> = upnpStateStore.state
}
