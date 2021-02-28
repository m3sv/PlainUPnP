package com.m3sv.plainupnp.common

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

// TODO move to a separate module
interface FilterDelegate {
    val state: Flow<String>
    suspend fun filter(text: String)
}

@Singleton
class Filter @Inject constructor() : FilterDelegate {

    private val textChannel = MutableSharedFlow<String>()

    override val state: Flow<String> = textChannel.filterNotNull()

    override suspend fun filter(text: String) {
        textChannel.emit(text)
    }
}
