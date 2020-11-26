package com.m3sv.plainupnp.upnp.volume

import kotlinx.coroutines.flow.Flow


interface UpnpVolumeManager {
    val volumeFlow: Flow<Int>
    suspend fun raiseVolume(step: Int)
    suspend fun lowerVolume(step: Int)
    suspend fun muteVolume(mute: Boolean)
    suspend fun setVolume(volume: Int)
    suspend fun getVolume(): Int
}
