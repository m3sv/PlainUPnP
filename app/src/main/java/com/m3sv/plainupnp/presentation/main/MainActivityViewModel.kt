package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.common.utils.disposeBy
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.upnp.UpnpManager
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val manager: UpnpManager,
                                                private val upnpPlayClickedUseCase: UpnpPlayClickedUseCase) :
        BaseViewModel<MainCommand, MainState>() {

    init {
        manager.resumeUpnpController()
    }

    private val discoveryDisposable: CompositeDisposable = CompositeDisposable()

    init {
        manager.renderers
                .map<MainState>(MainState::RenderersDiscovered)
                .mergeWith(manager.launchLocally.map(MainState::LaunchLocally))
                .mergeWith(manager.contentDirectories.map(MainState::ContentDirectoriesDiscovered))
                .mergeWith(manager.upnpRendererState.map(MainState::UpdateRendererState))
                .mergeWith(manager.renderedItem.map(MainState::RenderItem))
                .subscribe(this::updateState)
                .disposeBy(discoveryDisposable)
    }

    override fun execute(command: MainCommand) = when (command) {
        is MainCommand.ResumeUpnp -> manager.resumeRendererUpdate()
        is MainCommand.PauseUpnp -> manager.pauseRendererUpdate()
        is MainCommand.MoveTo -> manager.moveTo(command.progress)
        is MainCommand.SelectContentDirectory -> manager.selectContentDirectory(command.position)
        is MainCommand.SelectRenderer -> manager.selectRenderer(command.position)
        is MainCommand.PlayClicked -> upnpPlayClickedUseCase.execute()
        is MainCommand.NextClicked -> manager.playNext()
        is MainCommand.PreviousClicked -> manager.playPrevious()
        is MainCommand.Navigate -> when (command.route) {
            is Route.Back -> manager.browsePrevious()
            is Route.To -> {
                // no-op}
            }
        }
    }
}
