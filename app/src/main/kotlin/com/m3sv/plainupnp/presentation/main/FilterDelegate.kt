package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.common.Consumable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject
import javax.inject.Singleton

interface FilterDelegate {
    val state: Flow<Consumable<String>>
    suspend fun filter(text: String)
}

@ExperimentalCoroutinesApi
@Singleton
class Filter @Inject constructor() : FilterDelegate {

    private val _state = BroadcastChannel<Consumable<String>>(BUFFERED)

    override val state: Flow<Consumable<String>> = _state.asFlow()

    override suspend fun filter(text: String) {
        _state.send(Consumable(text))
    }
}
