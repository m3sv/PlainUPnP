package com.m3sv.plainupnp.presentation.main

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FilterDelegate @Inject constructor() {

    private val _state = BroadcastChannel<String>(BUFFERED)

    val state: Flow<String> = _state.asFlow()

    suspend fun filter(text: String) {
        _state.send(text)
    }
}
