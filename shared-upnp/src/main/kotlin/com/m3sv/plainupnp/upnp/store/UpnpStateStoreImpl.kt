package com.m3sv.plainupnp.upnp.store

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject

@ExperimentalCoroutinesApi
class UpnpStateStoreImpl @Inject constructor() :
    UpnpStateStore {
    private val contentChannel = BroadcastChannel<ContentState>(1)
    private var currentState: ContentState? = null

    override val state: Flow<ContentState> = contentChannel.asFlow()

    override fun setState(state: ContentState) {
        currentState = state
        contentChannel.offer(state)
    }

    override fun getCurrentState(): ContentState? = currentState
}
