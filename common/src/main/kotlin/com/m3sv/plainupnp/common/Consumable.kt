package com.m3sv.plainupnp.common

import java.util.concurrent.atomic.AtomicReference

class Consumable<T>(value: T? = null) {
    private val atomicReference = AtomicReference(value)

    fun consume(block: (T) -> Unit) {
        val value = atomicReference.getAndSet(null)
        if (value != null) {
            block(value)
        }
    }
}
