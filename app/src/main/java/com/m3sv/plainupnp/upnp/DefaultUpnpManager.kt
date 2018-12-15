package com.m3sv.plainupnp.upnp

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.bumptech.glide.request.RequestOptions
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.Event
import com.m3sv.plainupnp.common.RxBus
import com.m3sv.plainupnp.data.upnp.*
import com.m3sv.plainupnp.upnp.observables.ContentDirectoryDiscoveryObservable
import com.m3sv.plainupnp.upnp.observables.RendererDiscoveryObservable
import io.reactivex.BackpressureStrategy
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import com.m3sv.plainupnp.upnp.didl.ClingAudioItem
import com.m3sv.plainupnp.upnp.didl.ClingImageItem
import com.m3sv.plainupnp.upnp.didl.ClingVideoItem
import org.droidupnp.legacy.upnp.Factory
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * First is uri to a file, second is a title and third is an artist
 */
typealias RenderedItem = Triple<String?, String, RequestOptions>

class DefaultUpnpManager constructor(
    private val controller: UpnpServiceController,
    val factory: Factory
) : Observer, UpnpManager {

    override val rendererDiscoveryObservable =
        RendererDiscoveryObservable(controller.rendererDiscovery)

    override val contentDirectoryDiscoveryObservable =
        ContentDirectoryDiscoveryObservable(controller.contentDirectoryDiscovery)

    private val selectedDirectory = PublishSubject.create<Directory>()

    override val selectedDirectoryObservable: io.reactivex.Observable<Directory>
        get() = selectedDirectory.toFlowable(BackpressureStrategy.LATEST).toObservable()

    private var rendererCommand: RendererCommand? = null

    private val _rendererState: MutableLiveData<RendererState> = MutableLiveData()

    override val rendererState: LiveData<RendererState>
        get() = _rendererState

    private val _renderedItem: MutableLiveData<RenderedItem> = MutableLiveData()

    private var upnpRendererState: org.droidupnp.legacy.cling.UpnpRendererState? = null

    private var upnpRenderStateDisposable: Disposable? = null

    override val renderedItem: LiveData<RenderedItem>
        get() = _renderedItem

    override val currentContentDirectory: UpnpDevice?
        get() = controller.selectedContentDirectory

    override fun addObservers() = controller.run {
        addSelectedContentDirectoryObserver(this@DefaultUpnpManager)
    }

    override fun removeObservers() = controller.run {
        delSelectedContentDirectoryObserver(this@DefaultUpnpManager)
    }

    private val _contentData = MutableLiveData<List<DIDLObjectDisplay>>()

    override val contentData: LiveData<List<DIDLObjectDisplay>> = _contentData

    private val contentCallback: ContentCallback = { _contentData.postValue(it) }

    override fun selectContentDirectory(contentDirectory: UpnpDevice?) {
        Timber.d("Selected content directory: ${contentDirectory?.displayString}")

        val browseHome = controller.selectedContentDirectory != contentDirectory

        controller.selectedContentDirectory = contentDirectory

        if (browseHome)
            browseHome()
    }

    override fun selectRenderer(renderer: UpnpDevice?) {
        Timber.d("Selected renderer: ${renderer?.displayString}")
        controller.selectedRenderer = renderer
    }

    private var directoriesStructure = LinkedList<Directory>()

    private var next: Int = -1
    private var previous: Int = -1

    override val renderItemRelay: Relay<RenderItem> = PublishRelay.create()

    init {
        renderItemRelay.throttleFirst(1, TimeUnit.SECONDS).subscribe(::renderItem, Timber::e)
    }

    private fun renderItem(item: RenderItem) {
        rendererCommand?.commandStop()
        rendererCommand?.pause()

        next = item.position + 1
        previous = item.position - 1

        upnpRenderStateDisposable?.takeIf { !it.isDisposed }?.dispose()
        upnpRendererState = factory.createRendererState()

        upnpRenderStateDisposable = upnpRendererState?.let { rendererState ->
            rendererState.subscribeBy(onNext = {
                val durationRemaining = it.remainingDuration
                val durationElapse = it.elapsedDuration
                val progress = it.progress
                val title = it.title
                val artist = it.artist
                val state = it.state

                val newRendererState = RendererState(
                    durationRemaining,
                    durationElapse,
                    progress,
                    title,
                    artist,
                    state
                )

                Timber.i("New renderer state: $newRendererState")
                _rendererState.postValue(newRendererState)
            }, onError = Timber::e)
        }

        val requestOptions = when (item.item) {
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

        _renderedItem.postValue(RenderedItem(item.item.uri, item.item.title, requestOptions))

        rendererCommand = factory
            .createRendererCommand(upnpRendererState)
            ?.also {
                if (item.item !is ClingImageItem)
                    it.resume()
                it.updateFull()
                it.launchItem(item.item)
            }
    }

    override fun playNext() {
        _contentData.value?.takeIf { it.size > next && next != -1 }?.let {
            renderItem(RenderItem(it[next].didlObject as DIDLItem, next))
        }
    }

    override fun playPrevious() {
        _contentData.value?.takeIf { previous >= 0 && previous < it.size }?.let {
            renderItem(RenderItem(it[previous].didlObject as DIDLItem, previous))
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
                directoriesStructure = LinkedList<Directory>().also {
                    it.add(
                        Directory.Home
                    )
                }
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
        Timber.d("Browse previous: $element")
    }

    override fun update(o: Observable?, arg: Any?) {
        Timber.d("Selected new content directory: ${controller.selectedContentDirectory}")
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
        Timber.d("Resume UPnP controller")
        controller.resume()
    }

    override fun pauseUpnpController() {
        Timber.d("Pause UPnP controller")
        controller.pause()
    }
}