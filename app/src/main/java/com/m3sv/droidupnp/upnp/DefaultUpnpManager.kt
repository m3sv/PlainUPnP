package com.m3sv.droidupnp.upnp

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.bumptech.glide.request.RequestOptions
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.data.Directory
import com.m3sv.droidupnp.data.UpnpDevice
import com.m3sv.droidupnp.data.RendererState
import com.m3sv.droidupnp.upnp.observers.ContentDirectoryDiscoveryObservable
import com.m3sv.droidupnp.upnp.observers.RendererDiscoveryObservable
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject
import org.droidupnp.controller.upnp.UpnpServiceController
import org.droidupnp.model.cling.didl.ClingAudioItem
import org.droidupnp.model.cling.didl.ClingImageItem
import org.droidupnp.model.cling.didl.ClingVideoItem
import org.droidupnp.model.upnp.*
import org.droidupnp.model.upnp.didl.IDIDLItem
import timber.log.Timber
import java.util.*


/**
 * First is uri to a file, second is a title and third is an artist
 */
typealias RenderedItem = Triple<String, String, RequestOptions>

class DefaultUpnpManager constructor(val controller: UpnpServiceController, val factory: Factory) :
    Observer, UpnpManager {

    override val rendererDiscoveryObservable =
        RendererDiscoveryObservable(controller.rendererDiscovery)

    override val contentDirectoryDiscoveryObservable =
        ContentDirectoryDiscoveryObservable(controller.contentDirectoryDiscovery)

    private val selectedDirectory = PublishSubject.create<Directory>()

    override val selectedDirectoryObservable: io.reactivex.Observable<Directory>
        get() = selectedDirectory.toFlowable(BackpressureStrategy.LATEST).toObservable()

    private var rendererCommand: IRendererCommand? = null

    private val _rendererState: MutableLiveData<RendererState> = MutableLiveData()

    override val rendererState: LiveData<RendererState>
        get() = _rendererState

    private val _renderedItem: MutableLiveData<RenderedItem> = MutableLiveData()

    private var upnpRendererState: AUpnpRendererState? = null

    override val renderedItem: LiveData<RenderedItem>
        get() = _renderedItem

    override fun addObservers() = controller.run {
        addSelectedContentDirectoryObserver(this@DefaultUpnpManager)
    }

    override fun removeObservers() = controller.run {
        delSelectedContentDirectoryObserver(this@DefaultUpnpManager)
    }

    private val _contentData = MutableLiveData<List<DIDLObjectDisplay>>()

    override val contentData: LiveData<List<DIDLObjectDisplay>> = _contentData

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

    override fun selectContentDirectory(contentDirectory: UpnpDevice?) {
        Timber.d("Selected contentDirectory: ${contentDirectory?.displayString}")
        controller.selectedContentDirectory = contentDirectory
    }

    override fun selectRenderer(renderer: UpnpDevice?) {
        Timber.d("Selected renderer: ${renderer?.displayString}")
        controller.selectedRenderer = renderer
    }

    private var directoriesStructure = LinkedList<Directory>()

    private var next: Int = -1
    private var previous: Int = -1

    override fun renderItem(item: IDIDLItem, position: Int) {
        rendererCommand?.pause()

        next = position + 1
        previous = position - 1

        upnpRendererState = factory.createRendererState().also {
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

        val requestOptions = when (item) {
            is ClingImageItem -> {
                RequestOptions()
            }

            is ClingVideoItem -> {
                RequestOptions()
            }

            is ClingAudioItem -> {
                RequestOptions().placeholder(R.drawable.ic_music_note)
            }

            else -> RequestOptions()
        }
        _renderedItem.postValue(Triple(item.uri, item.title, requestOptions))
        rendererCommand = factory.createRendererCommand(upnpRendererState).also {
            it.resume()
            it.updateFull()
            it.launchItem(item)
        }
    }

    override fun playNext() {
        _contentData.value?.takeIf { it.size > next && next != -1 }?.let {
            renderItem(it[next].didlObject as IDIDLItem, next)
        }
    }

    override fun playPrevious() {
        _contentData.value?.takeIf { previous >= 0 && previous < it.size }?.let {
            renderItem(it[previous].didlObject as IDIDLItem, previous)
        }
    }

    override fun resumeRendererUpdate() {
        rendererCommand?.resume()
    }

    override fun pauseRendererUpdate() {
        rendererCommand?.pause()
    }

    override fun pausePlayback() = rendererCommand?.commandPause()

    override fun stopPlayback() {
        rendererCommand?.commandStop()
    }

    override fun resumePlayback() {
        rendererCommand?.commandPlay()
    }

    override fun browseHome() {
        browseTo("0", null)
    }

    override fun browseTo(id: String, parentId: String?, addToStructure: Boolean) {
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

    override fun browsePrevious() {
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

    override fun update(o: Observable?, arg: Any?) {
    }

    override fun moveTo(progress: Int, max: Int) {
        fun formatTime(h: Long, m: Long, s: Long): String {
            return ((if (h >= 10) "" + h else "0$h") + ":" + (if (m >= 10) "" + m else "0$m") + ":"
                    + if (s >= 10) "" + s else "0$s")
        }

        upnpRendererState?.run {
            val t = ((1.0 - (max.toDouble() - progress) / max) * durationSeconds).toLong()
            val h = t / 3600
            val m = (t - h * 3600) / 60
            val s = t - h * 3600 - m * 60
            val seek = formatTime(h, m, s)
            rendererCommand?.run {
                Timber.d("Seek to $seek")
                commandSeek(seek)
            }
        }
    }

    override fun resumeUpnpController() {
        controller.resume()
    }

    override fun pauseUpnpController() {
        controller.pause()
    }
}