package com.m3sv.plainupnp.common.util

import timber.log.Timber
import java.util.concurrent.TimeUnit

inline fun <T> benchmark(name: String, block: () -> T): T {
    val start = System.nanoTime()
    val result = block()
    val end = System.nanoTime()

    Timber.tag("PlainBenchmark").d("$name took ${TimeUnit.NANOSECONDS.toMillis(end - start)} ms")

    return result
}
