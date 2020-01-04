package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.Consumable
import com.m3sv.plainupnp.ContentCache
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.utils.disposeBy
import com.m3sv.plainupnp.common.utils.enforce
import com.m3sv.plainupnp.data.upnp.DIDLObjectDisplay
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.presentation.main.FilterDelegate
import com.m3sv.plainupnp.presentation.main.UpnpNavigationUseCase
import com.m3sv.plainupnp.upnp.ContentState
import com.m3sv.plainupnp.upnp.Destination
import com.m3sv.plainupnp.upnp.UpnpManager
import com.m3sv.plainupnp.upnp.didl.*
import com.m3sv.plainupnp.upnp.usecase.ObserveUpnpStateUseCase
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import javax.inject.Inject


class HomeViewModel @Inject constructor(
    private val manager: UpnpManager,
    private val upnpNavigationUseCase: UpnpNavigationUseCase,
    private val cache: ContentCache,
    private val filterDelegate: FilterDelegate,
    observeUpnpStateUseCase: ObserveUpnpStateUseCase
) : BaseViewModel<HomeIntention, HomeState>(HomeState.Initial) {

    init {
        observeUpnpStateUseCase
            .execute()
            .subscribe { upnpState ->
                val newState = when (upnpState) {
                    is ContentState.Loading -> HomeState.Loading
                    is ContentState.Success ->
                        HomeState.Success(
                            upnpState.directoryName,
                            mapItems(upnpState.content),
                            upnpState.isRoot,
                            Consumable("")
                        )
                }

                updateState { newState }
            }.disposeBy(disposables)

        launch {
            filterDelegate.state.consumeEach { text ->
                updateState { previousState ->
                    when (previousState) {
                        is HomeState.Success -> previousState.copy(filterText = Consumable(text))
                        else -> previousState
                    }
                }
            }
        }
    }

    private fun mapItems(items: List<DIDLObjectDisplay>): List<ContentItem> = items.map { item ->
        when (item.didlObject) {
            is ClingDIDLContainer -> {
                ContentItem(
                    item.didlObject.id,
                    item.didlObject.id,
                    item.title,
                    ContentType.DIRECTORY,
                    icon = R.drawable.ic_folder
                )
            }

            is ClingImageItem -> {
                ContentItem(
                    (item.didlObject as ClingDIDLItem).uri,
                    checkCacheForExistence(item),
                    item.title,
                    ContentType.IMAGE,
                    icon = R.drawable.ic_image
                )
            }

            is ClingVideoItem -> {
                ContentItem(
                    (item.didlObject as ClingDIDLItem).uri,
                    checkCacheForExistence(item),
                    item.title,
                    ContentType.VIDEO,
                    icon = R.drawable.ic_video
                )
            }

            is ClingAudioItem -> {
                ContentItem(
                    (item.didlObject as ClingDIDLItem).uri,
                    checkCacheForExistence(item),
                    item.title,
                    ContentType.AUDIO,
                    icon = R.drawable.ic_music
                )
            }

            else -> throw IllegalStateException("Unknown DIDLObject")
        }
    }

    private fun checkCacheForExistence(item: DIDLObjectDisplay): String? =
        cache.get(item.didlObject.id) ?: (item.didlObject as ClingDIDLItem).uri

    override fun intention(intention: HomeIntention) {
        when (intention) {
            is HomeIntention.ItemClick -> manager.itemClick(intention.position)
            is HomeIntention.BackPress -> upnpNavigationUseCase.execute(Destination.Back)
        }.enforce
    }
}
