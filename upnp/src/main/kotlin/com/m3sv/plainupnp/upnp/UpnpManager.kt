package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

interface UpnpManager : UpnpNavigator {

    val renderers: Observable<List<DeviceDisplay>>

    val contentDirectories: Observable<List<DeviceDisplay>>

    val upnpRendererState: Flow<UpnpRendererState>

    fun startUpnpService()

    fun stopUpnpService()

    fun resumeRendererUpdate()

    fun pauseRendererUpdate()

    fun selectContentDirectory(position: Int)

    fun selectRenderer(position: Int)

    fun itemClick(position: Int)

    fun resumePlayback()

    fun pausePlayback()

    fun togglePlayback()

    fun stopPlayback()

    fun playNext()

    fun playPrevious()

    fun moveTo(progress: Int)

    fun raiseVolume()

    fun lowerVolume()

    fun dispose()

}
