package com.m3sv.plainupnp

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow

interface ShutdownNotifier {
    val flow: Flow<Unit>
}

@ExperimentalCoroutinesApi
object ShutdownNotifierImpl : ShutdownNotifier {

    private val shutdownChannel = BroadcastChannel<Unit>(Channel.BUFFERED)

    override val flow: Flow<Unit> = shutdownChannel.asFlow()

    fun shutdown() {
        shutdownChannel.offer(Unit)
    }
}
