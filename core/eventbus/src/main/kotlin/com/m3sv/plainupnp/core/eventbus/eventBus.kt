package com.m3sv.plainupnp.core.eventbus

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterIsInstance

abstract class Event(
    val data: Any?,
)

private val eventChannel: BroadcastChannel<Event> = BroadcastChannel(1)

val eventFlow: Flow<Event> = eventChannel.asFlow()

inline fun <reified T : Event> subscribe(): Flow<T> = eventFlow.filterIsInstance()

fun post(event: Event) {
    eventChannel.offer(event)
}

suspend fun sendEvent(event: Event) {
    eventChannel.send(event)
}


