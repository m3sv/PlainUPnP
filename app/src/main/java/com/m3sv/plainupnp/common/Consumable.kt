package com.m3sv.plainupnp.common

import java.util.concurrent.atomic.AtomicReference

class Consumable<T>(value: T) {
    private val atomicReference = AtomicReference(value)

    fun consume(): T? = atomicReference.getAndSet(null)
}