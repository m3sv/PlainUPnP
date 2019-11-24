package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.common.utils.disposeBy
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.presentation.base.ContentDirectory
import com.m3sv.plainupnp.presentation.base.Renderer
import com.m3sv.plainupnp.upnp.Destination
import com.m3sv.plainupnp.upnp.UpnpManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val manager: UpnpManager,
    private val upnpPlayClickedUseCase: UpnpPlayClickedUseCase,
    private val upnpNavigationUseCase: UpnpNavigationUseCase
) : BaseViewModel<MainIntention, MainState>() {

    init {

        with(manager) {
            Observable
                .combineLatest<List<Renderer>, List<ContentDirectory>, UpnpRendererState, MainState>(
                    observeRenderers(),
                    observeContentDirectories(),
                    upnpRendererState,
                    Function3(MainState::Render)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    launch { updateState(it) }
                }
                .disposeBy(disposables)
        }
    }

    private fun UpnpManager.observeContentDirectories() =
        contentDirectories.map { directories -> directories.map { ContentDirectory(it.device.friendlyName) } }

    private fun UpnpManager.observeRenderers() =
        renderers.map { renderers -> renderers.map { Renderer(it.device.friendlyName) } }

    override fun execute(intention: MainIntention) {
        Timber.d("Execute: $intention")
        return when (intention) {
            is MainIntention.ResumeUpnp -> manager.resumeRendererUpdate()
            is MainIntention.PauseUpnp -> manager.pauseRendererUpdate()
            is MainIntention.MoveTo -> manager.moveTo(intention.progress)
            is MainIntention.SelectContentDirectory -> manager.selectContentDirectory(intention.position)
            is MainIntention.SelectRenderer -> manager.selectRenderer(intention.position)
            is MainIntention.PlayerButtonClick -> {
                when (intention.button) {
                    PlayerButton.PLAY -> upnpPlayClickedUseCase.execute()
                    PlayerButton.PREVIOUS -> manager.playPrevious()
                    PlayerButton.NEXT -> manager.playNext()
                    PlayerButton.RAISE_VOLUME -> manager.raiseVolume()
                    PlayerButton.LOWER_VOLUME -> manager.lowerVolume()
                }
            }
            is MainIntention.Navigate -> when (intention.route) {
                is Route.Back -> upnpNavigationUseCase.execute(Destination.Back)
                is Route.To -> {
                    // no-op}
                }
            }
            MainIntention.StartUpnpService -> manager.startUpnpService()
            MainIntention.StopUpnpService -> manager.stopUpnpService()
        }
    }
}
