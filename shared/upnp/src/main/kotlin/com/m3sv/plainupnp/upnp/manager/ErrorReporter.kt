package com.m3sv.plainupnp.upnp.manager

import android.app.Application
import com.m3sv.plainupnp.common.Consumable
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject

class ErrorReporter @Inject constructor(private val application: Application) {

    // We use Conflated channel because error might happen before we actually observe errors
    private val errorChannel: BroadcastChannel<Consumable<String>> =
        BroadcastChannel(Channel.CONFLATED)

    val errorFlow = errorChannel.asFlow()

    fun report(errorReason: ErrorReason) {
        errorChannel.offer(Consumable(application.getString(errorReason.message)))
    }
}
