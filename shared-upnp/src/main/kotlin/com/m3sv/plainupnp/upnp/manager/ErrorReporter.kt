package com.m3sv.plainupnp.upnp.manager

import android.content.Context
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow

class ErrorReporter(private val context: Context) {

    private val errorChannel: BroadcastChannel<String> = BroadcastChannel(1)

    val errorFlow = errorChannel.asFlow()

    fun report(errorReason: ErrorReason) {
        errorChannel.offer(context.getString(errorReason.message))
    }
}
