package com.m3sv.plainupnp.core.eventbus

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance

abstract class Event(
    val data: Any?,
)

private val eventChannel: MutableSharedFlow<Event> = MutableSharedFlow()

val eventFlow: Flow<Event> = eventChannel

inline fun <reified T : Event> subscribe(): Flow<T> = eventFlow.filterIsInstance()

suspend fun post(event: Event) {
    eventChannel.emit(event)
}

