package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.DIDLObjectDisplay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject

interface UpnpStateStore {
    val state: Flow<ContentState>

    suspend fun setState(state: ContentState)
    suspend fun peekState(): ContentState?
}

sealed class UpnpDirectory(val content: List<DIDLObjectDisplay>) {
    object None : UpnpDirectory(listOf())

    class Root(
        val name: String,
        content: List<DIDLObjectDisplay>
    ) : UpnpDirectory(content)

    class SubUpnpDirectory(
        val parentName: String,
        content: List<DIDLObjectDisplay>
    ) : UpnpDirectory(content)
}

sealed class ContentState {
    object Loading : ContentState()
    data class Success(val upnpDirectory: UpnpDirectory) : ContentState()
}

@ExperimentalCoroutinesApi
class UpnpStateRepository @Inject constructor() : UpnpStateStore {

    private val contentChannel = BroadcastChannel<ContentState>(Channel.BUFFERED)
    private var currentState: ContentState? = null

    override val state: Flow<ContentState> = contentChannel.asFlow()

    override suspend fun setState(state: ContentState) {
        currentState = state
        contentChannel.offer(state)
    }

    override suspend fun peekState(): ContentState? = currentState
}
