package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.ContentCache
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.utils.enforce
import com.m3sv.plainupnp.data.upnp.DIDLObjectDisplay
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.upnp.ContentState
import com.m3sv.plainupnp.upnp.UpnpManager
import com.m3sv.plainupnp.upnp.didl.*
import com.m3sv.plainupnp.upnp.usecase.ObserveUpnpStateUseCase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class HomeViewModel @Inject constructor(private val manager: UpnpManager,
                                        private val observeUpnpStateUseCase: ObserveUpnpStateUseCase,
                                        private val cache: ContentCache) :
        BaseViewModel<MainFragmentIntention, MainFragmentState>() {

    init {
        launch {
            observeUpnpStateUseCase.execute().collect { state ->
                Timber.i("New home state: $state")
                updateState(when (state) {
                    is ContentState.Loading -> MainFragmentState.Loading
                    is ContentState.Success ->
                        MainFragmentState.Success(
                                state.directoryName,
                                mapItems(state.content)
                        )
                })
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

    override fun execute(intention: MainFragmentIntention) {
        when (intention) {
            is MainFragmentIntention.ItemClick -> {
                manager.itemClick(intention.position)
            }
        }.enforce
    }
}