package com.m3sv.plainupnp.upnp

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import javax.inject.Inject

interface UpnpStateStore {
    val state: ReceiveChannel<ContentState>

    suspend fun setState(state: ContentState)

    fun peekState(): ContentState?
}

class UpnpStateRepository @Inject constructor() : UpnpStateStore {

    private val _contentChannel = Channel<ContentState>()

    private var currentState: ContentState? = null

    override val state: ReceiveChannel<ContentState> = _contentChannel

    override suspend fun setState(state: ContentState) {
        currentState = state
        _contentChannel.send(state)
    }

    override fun peekState(): ContentState? = currentState
}