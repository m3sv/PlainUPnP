package com.m3sv.plainupnp.upnp.manager

import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.upnp.UpnpNavigator
import kotlinx.coroutines.flow.Flow

interface UpnpManager : UpnpNavigator {
    val renderers: Flow<List<DeviceDisplay>>
    val contentDirectories: Flow<List<DeviceDisplay>>
    val upnpRendererState: Flow<UpnpRendererState>

    fun selectContentDirectory(position: Int)
    fun selectRenderer(position: Int)
    suspend fun itemClick(position: Int)
    suspend fun resumePlayback()
    suspend fun pausePlayback()
    suspend fun togglePlayback()
    suspend fun stopPlayback()
    suspend fun playNext()
    suspend fun playPrevious()
    suspend fun seekTo(progress: Int)
}
