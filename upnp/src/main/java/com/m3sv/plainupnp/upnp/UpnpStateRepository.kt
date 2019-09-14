package com.m3sv.plainupnp.upnp

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import javax.inject.Inject

interface UpnpStateStore {
    val state: Flow<ContentState>

    suspend fun setState(state: ContentState)

    fun peekState(): ContentState?
}

class UpnpStateRepository @Inject constructor() : UpnpStateStore {

    private val _contentChannel = Channel<ContentState>()

    private var currentState: ContentState? = null

    override val state: Flow<ContentState> = _contentChannel.consumeAsFlow()

    override suspend fun setState(state: ContentState) {
        currentState = state
        _contentChannel.send(state)
    }

    override fun peekState(): ContentState? = currentState
}