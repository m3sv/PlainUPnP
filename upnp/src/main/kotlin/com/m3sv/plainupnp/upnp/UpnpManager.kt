package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import io.reactivex.Observable

interface UpnpManager : UpnpNavigator {
    val renderers: Observable<List<DeviceDisplay>>

    val contentDirectories: Observable<List<DeviceDisplay>>

    val upnpRendererState: Observable<UpnpRendererState>

    fun startUpnpService()

    fun stopUpnpService()

    fun resumeRendererUpdate()

    fun pauseRendererUpdate()

    fun selectContentDirectory(position: Int)

    fun selectRenderer(position: Int)

    fun itemClick(position: Int)

    fun resumePlayback()

    fun pausePlayback()

    fun stopPlayback()

    fun playNext()

    fun playPrevious()

    fun moveTo(progress: Int)

    fun raiseVolume()

    fun lowerVolume()

    fun dispose()
}
