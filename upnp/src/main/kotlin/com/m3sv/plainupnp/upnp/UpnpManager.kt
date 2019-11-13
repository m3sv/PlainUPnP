package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.DIDLItem
import com.m3sv.plainupnp.data.upnp.DIDLObjectDisplay
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import io.reactivex.Observable

sealed class ContentState {
    object Loading : ContentState()
    data class Success(
        val directoryName: String,
        val content: List<DIDLObjectDisplay>
    ) : ContentState()

    data class Exit(val root: Success) : ContentState()
}

data class RenderItem(
    val item: DIDLItem,
    val position: Int
)

data class LocalModel(
    val uri: String,
    val contentType: String
)

data class BrowseToModel(
    val id: String,
    val directoryName: String
)

interface UpnpManager {
    val renderers: Observable<List<DeviceDisplay>>

    val contentDirectories: Observable<List<DeviceDisplay>>

    val upnpRendererState: Observable<UpnpRendererState>

    fun itemClick(position: Int)

    fun selectContentDirectory(position: Int)

    fun selectRenderer(position: Int)

    fun resumeRendererUpdate()

    fun pauseRendererUpdate()

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
