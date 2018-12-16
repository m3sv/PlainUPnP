package com.m3sv.plainupnp.upnp

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.bumptech.glide.request.RequestOptions
import com.jakewharton.rxrelay2.PublishRelay
import com.jakewharton.rxrelay2.Relay
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.data.upnp.*
import com.m3sv.plainupnp.upnp.didl.ClingAudioItem
import com.m3sv.plainupnp.upnp.didl.ClingImageItem
import com.m3sv.plainupnp.upnp.observables.ContentDirectoryDiscoveryObservable
import com.m3sv.plainupnp.upnp.observables.RendererDiscoveryObservable
import io.reactivex.BackpressureStrategy
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
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
    private val factory: Factory
) : Observer, UpnpManager {

    private var upnpRendererState: org.droidupnp.legacy.cling.UpnpRendererState? = null

    private var rendererStateDisposable: Disposable? = null

    private var rendererCommand: RendererCommand? = null

    private val _rendererState: MutableLiveData<RendererState> = MutableLiveData()

    override val rendererState: LiveData<RendererState>
        get() = _rendererState

    private val _renderedItem: MutableLiveData<RenderedItem> = MutableLiveData()

    override val renderedItem: LiveData<RenderedItem>
        get() = _renderedItem

    override val currentContentDirectory: UpnpDevice?
        get() = controller.selectedContentDirectory

    private val _contentData = MutableLiveData<List<DIDLObjectDisplay>>()

    override val contentData: LiveData<List<DIDLObjectDisplay>> = _contentData

    override val rendererDiscovery = RendererDiscoveryObservable(controller.rendererDiscovery)

    override val contentDirectoryDiscovery =
        ContentDirectoryDiscoveryObservable(controller.contentDirectoryDiscovery)

    private val selectedDirectory = PublishSubject.create<Directory>()

    override val selectedDirectoryObservable: io.reactivex.Observable<Directory>
        get() = selectedDirectory.toFlowable(BackpressureStrategy.LATEST).toObservable()

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
        renderItemRelay.throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(::renderItem, Timber::e)
    }

    private fun renderItem(item: RenderItem) {
        rendererCommand?.commandStop()
        rendererCommand?.pause()
        rendererStateDisposable?.dispose()

        next = item.position + 1
        previous = item.position - 1

        upnpRendererState = factory.createRendererState()

        rendererStateDisposable = upnpRendererState?.map {
            val newRendererState = RendererState(
                it.remainingDuration,
                it.elapsedDuration,
                it.progress,
                it.title,
                it.artist,
                it.state
            )

            Timber.i("New renderer state: $newRendererState")
            newRendererState
        }?.subscribeBy(onNext = _rendererState::postValue, onError = Timber::e)

        rendererCommand = factory.createRendererCommand(upnpRendererState)
            ?.apply {
                if (item.item !is ClingImageItem)
                    resume()
                updateFull()
                launchItem(item.item)
            }

        val requestOptions = when (item.item) {
            is ClingAudioItem -> RequestOptions().placeholder(R.drawable.ic_music_note)
            else -> RequestOptions()
        }

        _renderedItem.postValue(RenderedItem(item.item.uri, item.item.title, requestOptions))
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
        factory.createContentDirectoryCommand()?.browse(id, null, _contentData::postValue)
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

    override fun addObservers() = controller.run {
        addSelectedContentDirectoryObserver(this@DefaultUpnpManager)
    }

    override fun removeObservers() = controller.run {
        delSelectedContentDirectoryObserver(this@DefaultUpnpManager)
    }
}