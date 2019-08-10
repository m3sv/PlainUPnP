package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.Consumable
import com.m3sv.plainupnp.common.utils.disposeBy
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.upnp.UpnpManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
        private val manager: UpnpManager,
        private val upnpPlayClickedUseCase: UpnpPlayClickedUseCase,
        private val launchLocallyUseCase: LaunchLocallyUseCase)
    : BaseViewModel<MainIntention, MainState>() {

    init {
        manager.resumeUpnpController()
    }

    private val discoveryDisposable: CompositeDisposable = CompositeDisposable()

    init {
        with(manager) {
            renderers
                    .map<MainState>(MainState::RenderersDiscovered)
                    .mergeWith(contentDirectories.map(MainState::ContentDirectoriesDiscovered))
                    .mergeWith(upnpRendererState.map(MainState::UpdateRendererState))
                    .mergeWith(renderedItem.map(MainState::RenderItem))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this@MainActivityViewModel::updateState)
                    .disposeBy(discoveryDisposable)

            launchLocally
                    .subscribeBy(
                            onNext = {
                                launchLocallyUseCase.execute(Consumable(it))
                            },
                            onError = Timber::e)
                    .disposeBy(discoveryDisposable)
        }
    }

    override fun execute(intention: MainIntention) = when (intention) {
        is MainIntention.ResumeUpnp -> manager.resumeRendererUpdate()
        is MainIntention.PauseUpnp -> manager.pauseRendererUpdate()
        is MainIntention.MoveTo -> manager.moveTo(intention.progress)
        is MainIntention.SelectContentDirectory -> manager.selectContentDirectory(intention.position)
        is MainIntention.SelectRenderer -> manager.selectRenderer(intention.position)
        is MainIntention.PlayClick -> upnpPlayClickedUseCase.execute()
        is MainIntention.RaiseVolume -> manager.raiseVolume()
        is MainIntention.LowerVolume -> manager.lowerVolume()
        is MainIntention.NextClick -> manager.playNext()
        is MainIntention.PreviousClick -> manager.playPrevious()
        is MainIntention.Navigate -> when (intention.route) {
            is Route.Back -> manager.browsePrevious()
            is Route.To -> {
                // no-op}
            }
        }
    }
}
