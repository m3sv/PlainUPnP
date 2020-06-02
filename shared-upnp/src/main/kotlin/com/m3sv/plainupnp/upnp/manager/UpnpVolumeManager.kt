package com.m3sv.plainupnp.upnp.manager

import com.m3sv.plainupnp.upnp.actions.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import javax.inject.Inject


interface UpnpVolumeManager {
    suspend fun raiseVolume(step: Int)
    suspend fun lowerVolume(step: Int)
    fun muteVolume(mute: Boolean)
    suspend fun setVolume(volume: Int)
    suspend fun getVolume(): Int
    fun observeVolume(): Flow<Int>
}

class UpnpVolumeManagerImpl @Inject constructor(
    private val raiseVolumeAction: RaiseVolumeAction,
    private val lowerVolumeAction: LowerVolumeAction,
    private val getVolumeAction: GetVolumeAction,
    private val setVolumeAction: SetVolumeAction,
    private val muteVolumeAction: MuteVolumeAction
) : UpnpVolumeManager {

    private val volumeChannel = BroadcastChannel<Int>(Channel.BUFFERED)

    override suspend fun raiseVolume(step: Int) = raiseVolumeAction(step).let(::postVolume)

    override suspend fun lowerVolume(step: Int) = lowerVolumeAction(step).let(::postVolume)

    override fun muteVolume(mute: Boolean) = muteVolumeAction(mute)

    override suspend fun setVolume(volume: Int) = setVolumeAction(volume).let(::postVolume)

    override suspend fun getVolume(): Int = getVolumeAction().also(::postVolume)

    private fun postVolume(volume: Int) {
        volumeChannel.offer(volume)
    }

    override fun observeVolume(): Flow<Int> = volumeChannel.asFlow()

}
