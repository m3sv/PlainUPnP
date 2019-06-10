package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.utils.disposeBy
import com.m3sv.plainupnp.common.utils.enforce
import com.m3sv.plainupnp.data.upnp.DIDLObjectDisplay
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.presentation.main.data.ContentType
import com.m3sv.plainupnp.presentation.main.data.Item
import com.m3sv.plainupnp.upnp.ContentState
import com.m3sv.plainupnp.upnp.UpnpManager
import com.m3sv.plainupnp.upnp.didl.*
import javax.inject.Inject


class MainFragmentViewModel @Inject constructor(private val manager: UpnpManager) :
        BaseViewModel<MainFragmentCommand, MainFragmentState>() {

    init {
        manager
                .content
                .map { state ->
                    when (state) {
                        is ContentState.Loading -> MainFragmentState.Loading
                        is ContentState.Success -> MainFragmentState.Success(state.directoryName, mapItems(state.content))
                    }
                }
                .subscribe(this::updateState)
                .disposeBy(disposables)
    }


    private fun mapItems(items: List<DIDLObjectDisplay>): List<Item> = items.map {
        when (it.didlObject) {
            is ClingDIDLContainer -> {
                Item(
                        it.didlObject.id,
                        it.title,
                        ContentType.DIRECTORY,
                        icon = R.drawable.ic_folder
                )
            }

            is ClingImageItem -> {
                Item(
                        (it.didlObject as ClingDIDLItem).uri,
                        it.title,
                        ContentType.IMAGE,
                        icon = R.drawable.ic_image
                )
            }

            is ClingVideoItem -> {
                Item(
                        (it.didlObject as ClingDIDLItem).uri,
                        it.title,
                        ContentType.VIDEO,
                        icon = R.drawable.ic_video
                )
            }

            is ClingAudioItem -> {
                Item((it.didlObject as ClingDIDLItem).uri,
                        it.title,
                        ContentType.AUDIO,
                        icon = R.drawable.ic_music
                )
            }

            else -> throw IllegalStateException("Unknown DIDLObject")
        }
    }

    override fun execute(command: MainFragmentCommand) {
        when (command) {
            is MainFragmentCommand.ItemClick -> {
                manager.itemClicked(command.position)
            }
        }.enforce
    }
}