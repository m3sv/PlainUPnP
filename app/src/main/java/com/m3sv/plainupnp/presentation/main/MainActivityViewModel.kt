package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.common.utils.disposeBy
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.upnp.UpnpManager
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(private val manager: UpnpManager)
    : BaseViewModel<MainCommand, MainState>() {

    init {
        manager.resumeUpnpController()
    }

    private val discoveryDisposable: CompositeDisposable = CompositeDisposable()

    init {
        manager.renderers
                .map(MainState::RenderersDiscovered)
                .subscribe(this::updateState)
                .disposeBy(discoveryDisposable)

        manager.contentDirectories
                .map(MainState::ContentDirectoriesDiscovered)
                .subscribe(this::updateState)
                .disposeBy(discoveryDisposable)

        manager.upnpRendererState
                .map(MainState::UpdateRendererState)
                .subscribe(this::updateState)
                .disposeBy(disposables)

        manager.renderedItem
                .map(MainState::RenderItem)
                .subscribe(this::updateState)
                .disposeBy(disposables)
    }

    override fun execute(command: MainCommand) {
        when (command) {
            is MainCommand.ResumeUpnp -> {
                manager.resumeRendererUpdate()
            }
            is MainCommand.PauseUpnp -> {
                manager.pauseRendererUpdate()
            }
            is MainCommand.MoveTo -> {
                manager.moveTo(command.progress)
            }

            is MainCommand.SelectContentDirectory -> {
                manager.selectContentDirectory(command.position)
            }

            is MainCommand.SelectRenderer -> {
                manager.selectRenderer(command.position)
            }

            is MainCommand.PlayClicked -> {

            }

            is MainCommand.NextClicked -> {
                manager.playNext()
            }

            is MainCommand.PreviousClicked -> {
                manager.playPrevious()
            }

            is MainCommand.Navigate -> {
                when (command.route) {
                    is Route.Back -> {
                        manager.browsePrevious()
                    }
                    is Route.To -> {
                        manager.browseTo(command.route.path)
                    }
                }
            }
        }.enforce
    }
}
