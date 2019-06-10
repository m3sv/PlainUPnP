package com.m3sv.plainupnp.upnp

import com.bumptech.glide.request.RequestOptions
import com.m3sv.plainupnp.data.upnp.*
import io.reactivex.Observable

data class RenderItem(val item: DIDLItem, val position: Int)

data class RenderedItem(val uri: String?, val title: String, val requestOptions: RequestOptions)

interface UpnpManager {
    val renderers: Observable<List<DeviceDisplay>>

    val contentDirectories: Observable<List<DeviceDisplay>>

    val selectedDirectoryObservable: Observable<Directory>

    val upnpRendererState: Observable<RendererState>

    val renderedItem: Observable<RenderedItem>

    val content: Observable<ContentState>

    val launchLocally: Observable<LocalModel>

    val currentContentDirectory: UpnpDevice?

    fun itemClicked(position: Int)

    fun renderItem(item: RenderItem)

    fun selectContentDirectory(position: Int)

    fun selectRenderer(position: Int)

    fun resumeUpnpController()

    fun pauseUpnpController()

    fun resumeRendererUpdate()

    fun pauseRendererUpdate()

    fun pausePlayback()

    fun stopPlayback()

    fun resumePlayback()

    fun playNext()

    fun playPrevious()

    fun browseHome()

    fun browsePrevious()

    fun moveTo(progress: Int, max: Int = 100)

    fun dispose()
}


data class LocalModel(val uri: String,
                      val contentType: String)

data class BrowseToModel(
        val id: String,
        val directoryName: String
)

sealed class ContentState {
    object Loading : ContentState()
    data class Success(val directoryName: String,
                       val content: List<DIDLObjectDisplay>) : ContentState()
}