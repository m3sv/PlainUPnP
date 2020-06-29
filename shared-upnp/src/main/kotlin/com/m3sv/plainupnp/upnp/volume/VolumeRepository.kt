package com.m3sv.plainupnp.upnp.volume

import com.m3sv.plainupnp.upnp.actions.renderingcontrol.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.fourthline.cling.model.meta.Service
import javax.inject.Inject

class VolumeRepository @Inject constructor(
    private val raiseVolumeAction: RaiseVolumeAction,
    private val lowerVolumeAction: LowerVolumeAction,
    private val getVolumeAction: GetVolumeAction,
    private val setVolumeAction: SetVolumeAction,
    private val muteVolumeAction: MuteVolumeAction
) {
    private val volumeChannel = BroadcastChannel<Int>(1)

    val volumeFlow: Flow<Int> = volumeChannel.asFlow()

    suspend fun raiseVolume(service: Service<*, *>, step: Int) =
        raiseVolumeAction(service, step).let(::postVolume)

    suspend fun lowerVolume(service: Service<*, *>, step: Int) =
        lowerVolumeAction(service, step).let(::postVolume)

    suspend fun muteVolume(service: Service<*, *>, mute: Boolean) = muteVolumeAction(service, mute)

    suspend fun setVolume(service: Service<*, *>, volume: Int) =
        setVolumeAction(service, volume).let(::postVolume)

    suspend fun getVolume(service: Service<*, *>): Int = getVolumeAction(service).also(::postVolume)

    private fun postVolume(volume: Int) {
        volumeChannel.offer(volume)
    }
}

