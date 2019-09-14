package com.m3sv.plainupnp.upnp


import com.m3sv.plainupnp.common.utils.formatTime
import com.m3sv.plainupnp.common.utils.throttle
import com.m3sv.plainupnp.data.upnp.*
import com.m3sv.plainupnp.upnp.didl.ClingAudioItem
import com.m3sv.plainupnp.upnp.didl.ClingDIDLContainer
import com.m3sv.plainupnp.upnp.didl.ClingImageItem
import com.m3sv.plainupnp.upnp.didl.ClingVideoItem
import com.m3sv.plainupnp.upnp.usecase.LaunchLocallyUseCase
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class DefaultUpnpManager @Inject constructor(
    override val renderers: RendererDiscoveryObservable,
    override val contentDirectories: ContentDirectoryDiscoveryObservable,
    private val controller: UpnpServiceController,
    private val factory: UpnpFactory,
    private val upnpNavigator: UpnpNavigator,
    private val launchLocallyUseCase: LaunchLocallyUseCase,
    private val upnpStateStore: UpnpStateStore
) : UpnpManager, CoroutineScope, UpnpNavigator by upnpNavigator {

    init {
        controller.resume()
    }

    override val coroutineContext: CoroutineContext = Dispatchers.Default + Job()

    private val rendererStateSubject = PublishSubject.create<UpnpRendererState>()

    override val upnpRendererState: Observable<UpnpRendererState> =
        rendererStateSubject.startWith(EmptyUpnpRendererState)

    private var upnpRendererStateObservable: UpnpRendererStateObservable? = null

    private var rendererCommand: RendererCommand? = null

    private var isLocal: Boolean = false

    private var next: Int = -1

    private var previous: Int = -1

    private val renderItem: Channel<RenderItem> = Channel()

    init {
        launch {
            renderItem.throttle(scope = this).collect {
                render(it)
            }
        }
    }

    override fun selectContentDirectory(position: Int) {
        if (position !in contentDirectories.currentContentDirectories().indices) {
            navigateTo(Destination.Home)
            return
        }

        val contentDirectory = contentDirectories.currentContentDirectories()[position].device

        if (controller.selectedContentDirectory != contentDirectory) {
            controller.selectedContentDirectory = contentDirectory
            navigateTo(Destination.Home)
        }
    }

    override fun selectRenderer(position: Int) {
        if (position !in renderers.currentRenderers().indices) {
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

    private fun renderItem(item: RenderItem) {
        launch {
            renderItem.send(item)
        }
    }

    private fun render(item: RenderItem) {
        Timber.d("Render item: ${item.item.uri}")

        rendererCommand?.run {
            pause()
            commandStop()
        }

        if (isLocal) {
            launchLocallyUseCase.execute(item)
            return
        }

        next = item.position + 1
        previous = item.position - 1

        with(item.item) {
            val type = when (this) {
                is ClingAudioItem -> UpnpItemType.AUDIO
                is ClingImageItem -> UpnpItemType.IMAGE
                is ClingVideoItem -> UpnpItemType.VIDEO
                else -> UpnpItemType.UKNOWN
            }

            upnpRendererStateObservable = UpnpRendererStateObservable(id, uri, type)
        }

        upnpRendererStateObservable?.doOnNext {
            if (it.state == UpnpRendererState.State.FINISHED)
                rendererCommand?.pause()
        }?.subscribe(rendererStateSubject)

        rendererCommand = factory.createRendererCommand(upnpRendererStateObservable)
            ?.apply {
                if (item.item !is ClingImageItem)
                    resume()

                launchItem(item.item)
            }
    }

    override fun playNext() {
        upnpStateStore.peekState()?.let {
            if (it is ContentState.Success
                && next in it.content.indices
                && it.content[next].didlObject is DIDLItem
            ) {
                renderItem(RenderItem(it.content[next].didlObject as DIDLItem, next))
            }
        }
    }

    override fun playPrevious() {
        upnpStateStore.peekState()?.let {
            if (it is ContentState.Success
                && previous in it.content.indices
                && it.content[previous].didlObject is DIDLItem
            ) {
                renderItem(RenderItem(it.content[previous].didlObject as DIDLItem, previous))
            }
        }
    }

    override fun resumeRendererUpdate() {
        Timber.v("Resume renderer update")
        rendererCommand?.resume()
    }

    override fun pauseRendererUpdate() {
        Timber.v("Pause renderer update")
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

    override fun moveTo(progress: Int, max: Int) {
        upnpRendererStateObservable?.run {
            rendererCommand?.run {
                formatTime(max, progress, durationSeconds)?.let {
                    commandSeek(it)
                }
            }
        }
    }

    override fun dispose() {
        coroutineContext.cancel()
    }

    override fun itemClick(position: Int) {
        upnpStateStore.peekState()?.let { state ->
            when (state) {
                is ContentState.Success -> {
                    if (position in state.content.indices) {
                        val item = state.content[position]

                        when (item.didlObject) {
                            is ClingDIDLContainer -> navigateTo(
                                Destination.Path(
                                    item.didlObject.id,
                                    item.title
                                )
                            )

                            else -> renderItem(
                                RenderItem(
                                    state.content[position].didlObject as DIDLItem,
                                    position
                                )
                            )
                        }
                    }
                }
                is ContentState.Loading -> {
                    // no-op
                }
            }
        }
    }

    override fun raiseVolume() {
        rendererCommand?.raiseVolume()
    }

    override fun lowerVolume() {
        rendererCommand?.lowerVolume()
    }
}