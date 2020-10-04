package com.m3sv.plainupnp.core.eventbus

import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filterIsInstance

abstract class Event(
    val data: Any?,
)

val eventChannel: BroadcastChannel<Event> = BroadcastChannel(1)

inline fun <reified T : Event> subscribe(): Flow<T> = eventChannel
    .asFlow()
    .filterIsInstance()

fun post(event: Event) {
    eventChannel.offer(event)
}


