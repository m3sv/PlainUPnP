package com.m3sv.plainupnp.upnp.manager

import android.app.Application
import com.m3sv.plainupnp.common.Consumable
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

class ErrorReporter @Inject constructor(private val application: Application) {

    private val errorChannel: MutableSharedFlow<Consumable<String>> = MutableSharedFlow()

    val errorFlow = errorChannel

    suspend fun report(errorReason: ErrorReason) {
        errorChannel.emit(Consumable(application.getString(errorReason.message)))
    }
}
