package com.m3sv.plainupnp.upnp

import android.arch.lifecycle.LiveData
import com.m3sv.plainupnp.data.Directory
import com.m3sv.plainupnp.data.RendererState
import com.m3sv.plainupnp.upnp.observers.ContentDirectoryDiscoveryObservable
import com.m3sv.plainupnp.upnp.observers.RendererDiscoveryObservable
import io.reactivex.Observable
import com.m3sv.plainupnp.data.UpnpDevice
import org.droidupnp.legacy.upnp.didl.IDIDLItem

interface UpnpManager {
    val rendererDiscoveryObservable: RendererDiscoveryObservable

    val contentDirectoryDiscoveryObservable: ContentDirectoryDiscoveryObservable

    val selectedDirectoryObservable: Observable<Directory>

    val rendererState: LiveData<RendererState>

    val renderedItem: LiveData<RenderedItem>

    val contentData: LiveData<List<DIDLObjectDisplay>>

    val currentContentDirectory: UpnpDevice?

    fun addObservers()

    fun removeObservers()

    fun selectContentDirectory(contentDirectory: UpnpDevice?)

    fun selectRenderer(renderer: UpnpDevice?)

    fun renderItem(item: IDIDLItem, position: Int)

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