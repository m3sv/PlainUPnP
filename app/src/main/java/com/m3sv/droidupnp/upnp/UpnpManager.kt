package com.m3sv.droidupnp.upnp

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.m3sv.droidupnp.upnp.observers.ContentDirectoryDiscoveryObservable
import com.m3sv.droidupnp.upnp.observers.RendererDiscoveryObservable
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject
import org.droidupnp.controller.upnp.UpnpServiceController
import org.droidupnp.model.upnp.DeviceDiscoveryObserver
import org.droidupnp.model.upnp.Factory
import org.droidupnp.model.upnp.IRendererCommand
import org.droidupnp.model.upnp.IUpnpDevice
import org.droidupnp.model.upnp.didl.IDIDLItem
import timber.log.Timber
import java.util.*


sealed class Directory {
    object Home : Directory()
    data class SubDirectory(val id: String, val parentId: String?) : Directory()
}

/**
 * First is uri to a file, second is a title and third is an artist
 */
typealias RenderedItem = Pair<String, String>

class UpnpManager constructor(val controller: UpnpServiceController, val factory: Factory) :
    DeviceDiscoveryObserver, Observer {

    val rendererDiscoveryObservable = RendererDiscoveryObservable(controller.rendererDiscovery)

    val contentDirectoryDiscoveryObservable =
        ContentDirectoryDiscoveryObservable(controller.contentDirectoryDiscovery)

    private val selectedDirectory = PublishSubject.create<Directory>()

    val selectedDirectoryObservable: io.reactivex.Observable<Directory>
        get() = selectedDirectory.toFlowable(BackpressureStrategy.LATEST).toObservable()

    private var rendererCommand: IRendererCommand? = null

    data class RendererState(
        val durationRemaining: String?,
        val durationElapse: String?,
        val progress: Int,
        val title: String?,
        val artist: String?,
        val state: org.droidupnp.model.upnp.RendererState.State
    )

    private val _rendererState: MutableLiveData<RendererState> = MutableLiveData()

    val rendererState: LiveData<RendererState>
        get() = _rendererState

    private val _renderedItem: MutableLiveData<RenderedItem> = MutableLiveData()

    val renderedItem: LiveData<RenderedItem>
        get() = _renderedItem

    fun addObservers() = controller.run {
        rendererDiscovery.addObserver(this@UpnpManager)
        contentDirectoryDiscovery.addObserver(this@UpnpManager)
        addSelectedContentDirectoryObserver(this@UpnpManager)
    }

    fun removeObservers() = controller.run {
        rendererDiscovery.removeObserver(this@UpnpManager)
        contentDirectoryDiscovery.removeObserver(this@UpnpManager)
        delSelectedContentDirectoryObserver(this@UpnpManager)
    }

    private val _contentData = MutableLiveData<List<DIDLObjectDisplay>>()

    val contentData: LiveData<List<DIDLObjectDisplay>> = _contentData

    private val contentCallback: ContentCallback =
        object : ContentCallback {
            private var content: List<DIDLObjectDisplay>? = null

            override fun setContent(content: ArrayList<DIDLObjectDisplay>) {
                _contentData.postValue(content)
            }

            override fun run() {
                Timber.d("content size: ${content?.size ?: 0}")
            }
        }

    fun selectContentDirectory(contentDirectory: IUpnpDevice?) {
        Timber.d("Selected contentDirectory: ${contentDirectory?.displayString}")
        controller.selectedContentDirectory = contentDirectory
    }

    fun selectRenderer(renderer: IUpnpDevice?) {
        Timber.d("Selected renderer: ${renderer?.displayString}")
        controller.selectedRenderer = renderer
    }

    private var directoriesStructure = LinkedList<Directory>()

    fun renderItem(item: IDIDLItem) {
        rendererCommand?.pause()
        val rendererState = factory.createRendererState().also {
            it.addObserver { _, _ ->
                val durationRemaining = it.remainingDuration
                val durationElapse = it.position
                val progress = it.elapsedPercent
                val title = it.title
                val artist = it.artist
                val state = it.state

                val rendererState = RendererState(
                    durationRemaining,
                    durationElapse,
                    progress,
                    title,
                    artist,
                    state
                )

                Timber.i("New renderer state: $rendererState")
                _rendererState.postValue(rendererState)
            }

        }

        _renderedItem.postValue(Pair(item.uri, item.title))
        rendererCommand = factory.createRendererCommand(rendererState).also {
            it.resume()
            it.updateFull()
            it.launchItem(item)
        }
    }

    fun resumeRendererUpdate() {
        rendererCommand?.resume()
    }

    fun pauseRendererUpdate() {
        rendererCommand?.pause()
    }

    fun pausePlayback() = rendererCommand?.commandPause()

    fun stopPlayback() {
        rendererCommand?.commandStop()
    }

    fun resumePlayback() {
        rendererCommand?.commandPlay()
    }

    override fun addedDevice(device: IUpnpDevice?) {
    }

    fun browseHome() {
        browseTo("0", null)
    }

    fun browseTo(id: String, parentId: String?, addToStructure: Boolean = true) {
        Timber.d("Browse: $id")
        factory.createContentDirectoryCommand()?.browse(id, null, contentCallback)
        when (id) {
            "0" -> {
                selectedDirectory.onNext(Directory.Home)
                directoriesStructure = LinkedList<Directory>().also { it.add(Directory.Home) }
            }
            else -> {
                val subDirectory = Directory.SubDirectory(id, parentId)
                selectedDirectory.onNext(subDirectory)
                if (addToStructure)
                    directoriesStructure.addFirst(subDirectory)
                Timber.d("Adding subdirectory: $subDirectory")
            }
        }
    }

    fun browsePrevious() {
        val element = directoriesStructure.pop()
        when (element) {
            is Directory.Home -> {
                browseTo("0", null)
            }
            is Directory.SubDirectory -> {
                browseTo(element.parentId!!, element.parentId, false)
            }
        }
        Timber.d(element.toString())
    }

    override fun removedDevice(device: IUpnpDevice?) {
        Timber.d("Removed $device")
    }

    override fun update(o: Observable?, arg: Any?) {
    }
}