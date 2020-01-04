package com.m3sv.plainupnp.presentation.main

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.ReceiveChannel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterDelegate @Inject constructor() {

    private val _state = BroadcastChannel<String>(BUFFERED)

    val state: ReceiveChannel<String> = _state.openSubscription()

    suspend fun filter(text: String) {
        _state.send(text)
    }
}
