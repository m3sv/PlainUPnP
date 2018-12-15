package com.m3sv.plainupnp.upnp

import android.arch.lifecycle.LiveData
import com.jakewharton.rxrelay2.Relay
import com.m3sv.plainupnp.data.upnp.*
import com.m3sv.plainupnp.upnp.observables.ContentDirectoryDiscoveryObservable
import com.m3sv.plainupnp.upnp.observables.RendererDiscoveryObservable
import io.reactivex.Observable

interface UpnpManager {
    val rendererDiscovery: RendererDiscoveryObservable

    val contentDirectoryDiscovery: ContentDirectoryDiscoveryObservable

    val selectedDirectoryObservable: Observable<Directory>

    val rendererState: LiveData<RendererState>

    val renderedItem: LiveData<RenderedItem>

    val contentData: LiveData<List<DIDLObjectDisplay>>

    val currentContentDirectory: UpnpDevice?

    val renderItemRelay: Relay<RenderItem>

    fun addObservers()

    fun removeObservers()

    fun selectContentDirectory(contentDirectory: UpnpDevice?)

    fun selectRenderer(renderer: UpnpDevice?)

    fun resumeUpnpController()

    fun pauseUpnpController()

    fun resumeRendererUpdate()

    fun pauseRendererUpdate()

    fun pausePlayback(): Unit?

    fun stopPlayback()

    fun resumePlayback()

    fun playNext()

    fun playPrevious()

    fun browseHome()

    fun browseTo(id: String, parentId: String?, addToStructure: Boolean = true)

    fun browsePrevious()

    fun moveTo(progress: Int, max: Int)
}

data class RenderItem(val item: DIDLItem, val position: Int)