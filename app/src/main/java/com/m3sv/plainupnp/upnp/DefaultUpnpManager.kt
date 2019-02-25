package com.m3sv.plainupnp.upnp

import com.bumptech.glide.request.RequestOptions
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.utils.formatTime
import com.m3sv.plainupnp.data.upnp.*
import com.m3sv.plainupnp.upnp.didl.ClingAudioItem
import com.m3sv.plainupnp.upnp.didl.ClingImageItem
import com.m3sv.plainupnp.upnp.didl.ClingVideoItem
import com.m3sv.plainupnp.upnp.discovery.ContentDirectoryDiscoveryObservable
import com.m3sv.plainupnp.upnp.discovery.RendererDiscoveryObservable
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.droidupnp.legacy.cling.UpnpRendererStateObservable
import org.droidupnp.legacy.upnp.Factory
import timber.log.Timber
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit


/**
 * First is uri to a file, second is a title and third is an artist
 */
typealias RenderedItem = Triple<String?, String, RequestOptions>

class DefaultUpnpManager constructor(
        private val controller: UpnpServiceController,
        private val factory: Factory,
        override val rendererDiscovery: RendererDiscoveryObservable,
        override val contentDirectoryDiscovery: ContentDirectoryDiscoveryObservable
) : UpnpManager {

    private val rendererStateSubject = PublishSubject.create<RendererState>()

    override val upnpRendererState: Observable<RendererState> = rendererStateSubject

    private val renderedItemSubject = PublishSubject.create<RenderedItem>()

    override val renderedItem: Observable<RenderedItem> = renderedItemSubject

    private var contentState: ContentState? = null

    private val contentSubject = PublishSubject.create<ContentState>()

    override val content: Observable<ContentState> = contentSubject.doOnNext {
        contentState = it
    }

    override val currentContentDirectory: UpnpDevice?
        get() = controller.selectedContentDirectory

    private val launchLocallySubject: PublishSubject<LaunchLocally> = PublishSubject.create()

    override val launchLocally: Observable<LaunchLocally> = launchLocallySubject.toFlowable(BackpressureStrategy.LATEST).toObservable()

    private val selectedDirectory = PublishSubject.create<Directory>()

    override val selectedDirectoryObservable: Observable<Directory> = selectedDirectory.toFlowable(BackpressureStrategy.LATEST).toObservable()

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
        renderItem
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(::render, Timber::e)

        browseTo.doOnNext { contentSubject.onNext(ContentState.Loading) }
                .throttleLast(500, TimeUnit.MILLISECONDS)
                .subscribe(::browse, Timber::e)
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
        }?.subscribeBy(onNext = {
            rendererStateSubject.onNext(it)

            if (it.state == UpnpRendererState.State.STOP) {
                rendererCommand?.pause()
            }
        }, onError = Timber::e)

        rendererCommand = factory.createRendererCommand(upnpRendererStateObservable)
                ?.apply {
                    if (item.item !is ClingImageItem)
                        resume()
                    else
                        rendererStateSubject.onNext(RendererState(progress = 0, state = UpnpRendererState.State.STOP))
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
                launchLocallySubject.onNext(LaunchLocally(uri, it))
            }
        }
    }

    /**
     * Updates control sheet with latest launched item
     */
    private fun updateUi(toRender: RenderItem) {
        val requestOptions = when (toRender.item) {
            is ClingAudioItem -> RequestOptions().placeholder(R.drawable.ic_music_note)
            else -> RequestOptions()
        }

        renderedItemSubject.onNext(RenderedItem(toRender.item.uri, toRender.item.title, requestOptions))
    }

    override fun playNext() {
        contentState?.let {
            if (it is ContentState.Success
                    && next in 0 until it.content.size
                    && it.content[next].didlObject is DIDLItem) {
                renderItem(RenderItem(it.content[next].didlObject as DIDLItem, next))
            }
        }
    }

    override fun playPrevious() {
        contentState?.let {
            if (it is ContentState.Success
                    && previous in 0 until it.content.size
                    && it.content[previous].didlObject is DIDLItem) {
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

    override fun pausePlayback() {
        rendererCommand?.commandPause()
    }

    override fun stopPlayback() {
        rendererCommand?.commandStop()
    }

    override fun resumePlayback() {
        rendererCommand?.commandPlay()
    }

    override fun browseHome() {
        browseTo.onNext(BrowseToModel("0", currentContentDirectory?.friendlyName ?: "Home", null))
    }

    override fun browseTo(model: BrowseToModel) {
        browseTo.onNext(model)
    }

    private var browseFuture: Future<*>? = null

    private fun browse(model: BrowseToModel) {
        Timber.d("Browse: ${model.id}")

        browseFuture?.cancel(true)

        browseFuture = factory.createContentDirectoryCommand()?.browse(model.id, null) {
            contentSubject.onNext(ContentState.Success(model.directoryName, it ?: listOf()))

            when (model.id) {
                "0" -> {
                    selectedDirectory.onNext(Directory.Home(currentContentDirectory?.friendlyName
                            ?: "Home"))

                    directoriesStructure =
                            LinkedList<Directory>().apply { add(Directory.Home(model.directoryName)) }
                }
                else -> {
                    val subDirectory =
                            Directory.SubDirectory(model.id, model.directoryName, model.parentId)
                    selectedDirectory.onNext(subDirectory)
                    if (model.addToStructure)
                        directoriesStructure.addFirst(subDirectory)
                    Timber.d("Adding subdirectory: $subDirectory")
                }
            }
        }
    }

    override fun browsePrevious() {
        val element = if (!directoriesStructure.isEmpty())
            directoriesStructure.pop()
        else
            Directory.Home(currentContentDirectory?.friendlyName ?: "Home")

        when (element) {
            is Directory.Home -> browseHome()

            is Directory.SubDirectory -> {
                browseTo(
                        if (element.parentId == "0")
                            BrowseToModel("0", currentContentDirectory?.friendlyName
                                    ?: "Home", null)
                        else
                            BrowseToModel(
                                    element.parentId ?: "0",
                                    element.name,
                                    element.parentId,
                                    false
                            )
                )

            }
        }

        Timber.d("Browse previous: $element")
    }

    override fun moveTo(progress: Int, max: Int) {
        upnpRendererStateObservable?.run {
            rendererCommand?.run {
                formatTime(max, progress, durationSeconds)?.let {
                    Timber.d("Seek to $it")
                    commandSeek(it)
                }

            }
        }
    }

    override fun resumeUpnpController() {
        Timber.d("Resume UPnP upnpServiceController")
        controller.resume()
    }

    override fun pauseUpnpController() {
        Timber.d("Pause UPnP upnpServiceController")
        controller.pause()
    }
}