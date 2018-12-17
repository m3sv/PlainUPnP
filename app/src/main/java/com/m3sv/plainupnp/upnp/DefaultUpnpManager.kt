package com.m3sv.plainupnp.upnp

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.bumptech.glide.request.RequestOptions
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.data.upnp.*
import com.m3sv.plainupnp.upnp.didl.ClingAudioItem
import com.m3sv.plainupnp.upnp.didl.ClingImageItem
import com.m3sv.plainupnp.upnp.didl.ClingVideoItem
import com.m3sv.plainupnp.upnp.observables.ContentDirectoryDiscoveryObservable
import com.m3sv.plainupnp.upnp.observables.RendererDiscoveryObservable
import io.reactivex.BackpressureStrategy
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.droidupnp.legacy.cling.UpnpRendererStateObservable
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

    override val rendererDiscovery = RendererDiscoveryObservable(controller.rendererDiscovery)

    override val contentDirectoryDiscovery =
        ContentDirectoryDiscoveryObservable(controller.contentDirectoryDiscovery)

    private val _rendererState: MutableLiveData<RendererState> = MutableLiveData()

    override val rendererState: LiveData<RendererState>
        get() = _rendererState

    private val _renderedItem: MutableLiveData<RenderedItem> = MutableLiveData()

    override val renderedItem: LiveData<RenderedItem>
        get() = _renderedItem

    private val _contentData = MutableLiveData<ContentState>()

    override val contentData: LiveData<ContentState> = _contentData

    override val currentContentDirectory: UpnpDevice?
        get() = controller.selectedContentDirectory

    private val _launchLocally: PublishSubject<LaunchLocally> = PublishSubject.create()

    override val launchLocally: io.reactivex.Observable<LaunchLocally>
        get() = _launchLocally.toFlowable(BackpressureStrategy.LATEST).toObservable()

    private val selectedDirectory = PublishSubject.create<Directory>()

    override val selectedDirectoryObservable: io.reactivex.Observable<Directory>
        get() = selectedDirectory.toFlowable(BackpressureStrategy.LATEST).toObservable()

    private var upnpRendererStateObservable: UpnpRendererStateObservable? = null

    private var rendererStateDisposable: Disposable? = null

    private var rendererCommand: RendererCommand? = null

    private var isLocal: Boolean = false

    private var directoriesStructure = LinkedList<Directory>()

    private var next: Int = -1

    private var previous: Int = -1

    private val renderItem: Subject<RenderItem> = PublishSubject.create()

    private val browseTo: Subject<BrowseToModel> = PublishSubject.create()

    init {
        renderItem.throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(::render, Timber::e)
        browseTo.throttleLast(500, TimeUnit.MILLISECONDS).subscribe(::browse, Timber::e)
    }

    override fun selectContentDirectory(contentDirectory: UpnpDevice?) {
        Timber.d("Selected content directory: ${contentDirectory?.displayString}")

        val browseHome = controller.selectedContentDirectory != contentDirectory

        controller.selectedContentDirectory = contentDirectory

        if (browseHome)
            browseHome()
    }

    override fun selectRenderer(renderer: UpnpDevice?) {
        Timber.d("Selected renderer: ${renderer?.displayString}")

        if (renderer is LocalDevice) {
            isLocal = true
        } else {
            isLocal = false
            controller.selectedRenderer = renderer
        }
    }

    override fun renderItem(item: RenderItem) {
        renderItem.onNext(item)
    }

    private fun render(item: RenderItem) {
        rendererCommand?.run {
            commandStop()
            pause()
        }

        rendererStateDisposable?.dispose()

        updateUi(item)

        if (isLocal) {
            launchItemLocally(item)
            return
        }

        next = item.position + 1
        previous = item.position - 1

        upnpRendererStateObservable = factory.createRendererState()

        rendererStateDisposable = upnpRendererStateObservable?.map {
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

        rendererCommand = factory.createRendererCommand(upnpRendererStateObservable)
            ?.apply {
                if (item.item !is ClingImageItem)
                    resume()
                updateFull()
                launchItem(item.item)
            }
    }

    private fun launchItemLocally(item: RenderItem) {
        item.item.uri?.let { uri ->
            val contentType = when (item.item) {
                is ClingAudioItem -> "audio/*"
                is ClingImageItem -> "image/*"
                is ClingVideoItem -> "video/*"
                else -> null
            }

            contentType?.let {
                _launchLocally.onNext(LaunchLocally(uri, it))
            }
        }
    }

    /**
     * Updates control sheet with latest launched item
     */
    private fun updateUi(item: RenderItem) {
        val requestOptions = when (item.item) {
            is ClingAudioItem -> RequestOptions().placeholder(R.drawable.ic_music_note)
            else -> RequestOptions()
        }

        _renderedItem.postValue(RenderedItem(item.item.uri, item.item.title, requestOptions))
    }

    override fun playNext() {
        _contentData.value?.let {
            if (it is ContentState.Success && (next in 0..it.content.size)) {
                renderItem(RenderItem(it.content[next].didlObject as DIDLItem, next))
            }
        }
    }

    override fun playPrevious() {
        _contentData.value?.let {
            if (it is ContentState.Success && (previous in 0..it.content.size)) {
                renderItem(RenderItem(it.content[previous].didlObject as DIDLItem, previous))
            }
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
        browseTo.onNext(BrowseToModel("0", null))
    }

    override fun browseTo(model: BrowseToModel) {
        browseTo.onNext(model)
    }

    private fun browse(model: BrowseToModel) {
        Timber.d("Browse: ${model.id}")
        _contentData.postValue(ContentState.Loading)

        factory.createContentDirectoryCommand()?.browse(model.id, null) {
            _contentData.postValue(ContentState.Success(it ?: listOf()))
        }
        when (model.id) {
            "0" -> {
                selectedDirectory.onNext(Directory.Home)
                directoriesStructure = LinkedList<Directory>().apply { add(Directory.Home) }
            }
            else -> {
                val subDirectory = Directory.SubDirectory(model.id, model.parentId)
                selectedDirectory.onNext(subDirectory)
                if (model.addToStructure)
                    directoriesStructure.addFirst(subDirectory)
                Timber.d("Adding subdirectory: $subDirectory")
            }
        }
    }

    override fun browsePrevious() {
        val element = directoriesStructure.pop()
        when (element) {
            is Directory.Home -> {
                browseTo(BrowseToModel("0", null))
            }

            is Directory.SubDirectory -> {
                browseTo(BrowseToModel(element.parentId!!, element.parentId, false))
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

        upnpRendererStateObservable?.run {
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