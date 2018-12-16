package com.m3sv.plainupnp.common

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable


sealed class Event {
    class GetMovieSuggestionsEvent(val name: String) : Event()
}

object RxBus {
    private val publisher = PublishRelay.create<Event>()

    fun <T : Event> publish(event: T) = publisher.accept(event)

    fun <T : Event> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}

