package com.m3sv.plainupnp.presentation.main

import android.annotation.SuppressLint
import com.m3sv.plainupnp.data.upnp.RendererState
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.upnp.UpnpManager
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

@SuppressLint("CheckResult")
class UpnpPlayClickedUseCase @Inject constructor(private val manager: UpnpManager) {

    private var state: RendererState? = null

    init {
        manager.upnpRendererState
                .subscribeBy(onNext = {
                    state = it
                }, onError = Timber::e)
    }

    fun execute() {
        when (state?.state) {
            UpnpRendererState.State.STOP -> manager.resumePlayback()
            UpnpRendererState.State.PLAY -> manager.pausePlayback()
            UpnpRendererState.State.PAUSE -> manager.resumePlayback()
            UpnpRendererState.State.INITIALIZING -> manager.resumePlayback()
        }
    }
}