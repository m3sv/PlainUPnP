package com.m3sv.plainupnp.upnp


import com.bumptech.glide.request.RequestOptions
import com.m3sv.plainupnp.common.utils.disposeBy
import com.m3sv.plainupnp.common.utils.formatTime
import com.m3sv.plainupnp.data.upnp.*
import com.m3sv.plainupnp.upnp.didl.ClingAudioItem
import com.m3sv.plainupnp.upnp.didl.ClingDIDLContainer
import com.m3sv.plainupnp.upnp.didl.ClingImageItem
import com.m3sv.plainupnp.upnp.didl.ClingVideoItem
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * First is uri to a file, second is a title and third is an artist
 */
class DefaultUpnpManager @Inject constructor(
        private val controller: UpnpServiceController,
        private val factory: Factory,
        private val upnpNavigator: UpnpNavigator,
        override val renderers: RendererDiscoveryObservable,
        override val contentDirectories: ContentDirectoryDiscoveryObservable
) : UpnpManager, UpnpNavigator by upnpNavigator {

    private val disposables = CompositeDisposable()

    private val rendererStateSubject = PublishSubject.create<RendererState>()

    override val upnpRendererState: Observable<RendererState> = rendererStateSubject

    private val renderedItemSubject = PublishSubject.create<RenderedItem>()

    override val renderedItem: Observable<RenderedItem> = renderedItemSubject

    var contentState: ContentState? = null
        private set

    override val content: Observable<ContentState> = upnpNavigator.state.doOnNext {
        contentState = it
    }

    override val currentContentDirectory: UpnpDevice?
        get() = controller.selectedContentDirectory

    private val launchLocallySubject: PublishSubject<LocalModel> = PublishSubject.create()

    override val launchLocally: Observable<LocalModel> = launchLocallySubject.toFlowable(BackpressureStrategy.LATEST).toObservable()

    private val selectedDirectory = PublishSubject.create<Directory>()

    override val selectedDirectoryObservable: Observable<Directory> = selectedDirectory.toFlowable(BackpressureStrategy.LATEST).toObservable()

    private var upnpRendererStateObservable: UpnpRendererStateObservable? = null

    private var rendererStateDisposable: Disposable? = null

    private var rendererCommand: RendererCommand? = null

    private var isLocal: Boolean = false

    private var next: Int = -1

    private var previous: Int = -1

    private val renderItem: Subject<RenderItem> = PublishSubject.create()

    init {
        renderItem
                .throttleFirst(250, TimeUnit.MILLISECONDS)
                .subscribe(::render, Timber::e).disposeBy(disposables)
    }

    override fun selectContentDirectory(position: Int) {
        if (position !in 0 until contentDirectories.currentContentDirectories().size) {
            return
        }

        val contentDirectory = contentDirectories.currentContentDirectories()[position].device

        if (controller.selectedContentDirectory != contentDirectory) {
            controller.selectedContentDirectory = contentDirectory
            navigateHome()
        }
    }

    override fun selectRenderer(position: Int) {
        if (position !in 0 until renderers.currentRenderers().size) {
            return
        }

        val renderer = renderers.currentRenderers()[position].device

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
                launchLocallySubject.onNext(LocalModel(uri, it))
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
        navigateHome()
    }

    override fun browsePrevious() {
        upnpNavigator.navigatePrevious()
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
        controller.resume()
    }

    override fun pauseUpnpController() {
        controller.pause()
    }

    override fun dispose() {
        disposables.clear()
    }

    override fun itemClicked(position: Int) {
        contentState?.let { state ->
            when (state) {
                is ContentState.Success -> {
                    if (position in 0 until state.content.size) {
                        val item = state.content[position]

                        when (item.didlObject) {
                            is ClingDIDLContainer -> {
                                navigateTo(BrowseToModel(item.didlObject.id, item.title))
                            }

                            else -> renderItem(RenderItem(state.content[position].didlObject as DIDLItem, position))
                        }
                    }
                }
                is ContentState.Loading -> {
                    // no-op
                }
            }
        }
    }
}