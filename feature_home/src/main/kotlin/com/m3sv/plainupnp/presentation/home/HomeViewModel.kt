package com.m3sv.plainupnp.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.Consumable
import com.m3sv.plainupnp.common.utils.disposeBy
import com.m3sv.plainupnp.common.utils.enforce
import com.m3sv.plainupnp.data.upnp.DIDLObjectDisplay
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.presentation.main.FilterDelegate
import com.m3sv.plainupnp.upnp.ContentState
import com.m3sv.plainupnp.upnp.Destination
import com.m3sv.plainupnp.upnp.UpnpDirectory
import com.m3sv.plainupnp.upnp.didl.*
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import com.m3sv.plainupnp.upnp.usecase.ObserveUpnpStateUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


class HomeViewModel @Inject constructor(
    private val manager: UpnpManager,
    filterDelegate: FilterDelegate,
    private val observeUpnpStateUseCase: ObserveUpnpStateUseCase
) : BaseViewModel<HomeIntention>() {

    init {
        observeUpnpState()
    }

    // TODO Filtering must be done in a separate use case, refactor this
    @ExperimentalCoroutinesApi
    val filterText: LiveData<Consumable<String>> = filterDelegate.state.asLiveData()

    override fun intention(intention: HomeIntention) {
        when (intention) {
            is HomeIntention.ItemClick -> manager.itemClick(intention.position)
            is HomeIntention.BackPress -> manager.navigateTo(Destination.Back)
        }.enforce
    }

    private val mutableState = MutableLiveData<HomeState>()
    val state: LiveData<HomeState> = mutableState

    private fun observeUpnpState() {
        observeUpnpStateUseCase
            .execute()
            .map { contentState ->
                val newState = when (contentState) {
                    is ContentState.Loading -> HomeState.Loading
                    is ContentState.Success -> {
                        val directory = when (val directory = contentState.upnpDirectory) {
                            is UpnpDirectory.Root -> Directory.Root(
                                directory.name,
                                mapItems(directory.content)
                            )

                            is UpnpDirectory.SubUpnpDirectory -> Directory.SubDirectory(
                                directory.parentName,
                                mapItems(directory.content)
                            )

                            is UpnpDirectory.None -> Directory.None
                        }

                        HomeState.Success(directory)
                    }
                }

                newState
            }
            .subscribe { newState -> mutableState.postValue(newState) }
            .disposeBy(disposables)
    }

    private fun mapItems(items: List<DIDLObjectDisplay>): List<ContentItem> = items.map { item ->
        when (item.didlObject) {
            is ClingDIDLContainer -> {
                ContentItem(
                    item.didlObject.id,
                    item.title,
                    ContentType.FOLDER,
                    icon = R.drawable.ic_folder
                )
            }

            is ClingImageItem -> {
                ContentItem(
                    (item.didlObject as ClingDIDLItem).uri,
                    item.title,
                    ContentType.IMAGE,
                    icon = R.drawable.ic_bordered_image
                )
            }

            is ClingVideoItem -> {
                ContentItem(
                    (item.didlObject as ClingDIDLItem).uri,
                    item.title,
                    ContentType.VIDEO,
                    icon = R.drawable.ic_bordered_video
                )
            }

            is ClingAudioItem -> {
                ContentItem(
                    (item.didlObject as ClingDIDLItem).uri,
                    item.title,
                    ContentType.AUDIO,
                    icon = R.drawable.ic_bordered_music
                )
            }

            else -> throw IllegalStateException("Unknown DIDLObject")
        }
    }
}
