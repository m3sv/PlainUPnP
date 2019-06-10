package com.m3sv.plainupnp.common

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


sealed class Event {
    class GetMovieSuggestionsEvent(val name: String) : Event()
}

object RxBus {
    private val publisher = PublishSubject.create<Event>()

    fun <T : Event> publish(event: T) = publisher.onNext(event)

    fun <T : Event> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}

