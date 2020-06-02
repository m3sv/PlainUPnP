package com.m3sv.plainupnp.common.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <E> CoroutineScope.throttle(wait: Long = 200, channel: ReceiveChannel<E>) = produce {
    var nextTime = 0L
    channel.consumeEach {
        val curTime = System.currentTimeMillis()
        if (curTime >= nextTime) {
            nextTime = curTime + wait
            send(it)
        }
    }
}

fun <E> ReceiveChannel<E>.throttle(
        wait: Long = 200,
        scope: CoroutineScope
): Flow<E> = scope.run {
    flow {
        var nextTime = 0L
        consumeEach {
            val curTime = System.currentTimeMillis()
            if (curTime >= nextTime) {
                nextTime = curTime + wait

                emit(it)
            }
        }
    }
}

