package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.common.utils.disposeBy
import com.m3sv.plainupnp.data.upnp.UpnpRendererState
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.presentation.base.SpinnerItem
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
    private val filterDelegate: FilterDelegate
) : BaseViewModel<MainIntention, MainState>(MainState.Initial) {

    init {
        with(manager) {
            Observable
                .combineLatest<List<SpinnerItem>, List<SpinnerItem>, UpnpRendererState, MainState>(
                    observeRenderers(),
                    observeContentDirectories(),
                    upnpRendererState,
                    Function3(MainState::Render)
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { newState ->
                    launch { updateState { newState } }
                }
                .disposeBy(disposables)
        }
    }

    private fun UpnpManager.observeContentDirectories() =
        contentDirectories.map { directories -> directories.map { SpinnerItem(it.device.friendlyName) } }

    private fun UpnpManager.observeRenderers() =
        renderers.map { renderers -> renderers.map { SpinnerItem(it.device.friendlyName) } }

    override fun intention(intention: MainIntention) {
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
            is MainIntention.StartUpnpService -> manager.startUpnpService()
            is MainIntention.StopUpnpService -> manager.stopUpnpService()
            is MainIntention.Filter -> filterText(intention.text)
        }
    }

    private fun filterText(text: String) {
        launch { filterDelegate.filter(text) }
    }
}
