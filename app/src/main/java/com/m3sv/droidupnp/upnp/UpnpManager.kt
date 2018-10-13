package com.m3sv.droidupnp.upnp

import android.arch.lifecycle.LiveData
import com.m3sv.droidupnp.upnp.observers.ContentDirectoryDiscoveryObservable
import com.m3sv.droidupnp.upnp.observers.RendererDiscoveryObservable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.droidupnp.model.upnp.IUpnpDevice
import org.droidupnp.model.upnp.didl.IDIDLItem


interface UpnpManager {
    val rendererDiscoveryObservable: RendererDiscoveryObservable

    val contentDirectoryDiscoveryObservable: ContentDirectoryDiscoveryObservable

    val selectedDirectoryObservable: Observable<Directory>

    val rendererState: LiveData<DefaultUpnpManager.RendererState>

    val renderedItem: LiveData<RenderedItem>

    fun addObservers()

    fun removeObservers()

    fun selectContentDirectory(contentDirectory: IUpnpDevice?)

    fun selectRenderer(renderer: IUpnpDevice?)

    fun renderItem(item: IDIDLItem, position: Int)

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