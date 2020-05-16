package com.m3sv.plainupnp.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.m3sv.plainupnp.common.Consumable
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.common.Mapper
import com.m3sv.plainupnp.common.utils.enforce
import com.m3sv.plainupnp.presentation.base.BaseViewModel
import com.m3sv.plainupnp.upnp.ContentState
import com.m3sv.plainupnp.upnp.Destination
import com.m3sv.plainupnp.upnp.UpnpDirectory
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import com.m3sv.plainupnp.upnp.usecase.ObserveUpnpStateUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
class HomeViewModel @Inject constructor(
    private val manager: UpnpManager,
    private val upnpDirectoryMapper: Mapper<UpnpDirectory, Directory>,
    filterDelegate: FilterDelegate,
    observeUpnpStateUseCase: ObserveUpnpStateUseCase
) : BaseViewModel<HomeIntention>() {

    // TODO Filtering must be done in a separate use case, refactor this
    val filterText: LiveData<Consumable<String>> = filterDelegate.state.asLiveData()

    override fun intention(intention: HomeIntention) {
        viewModelScope.launch {
            when (intention) {
                is HomeIntention.ItemClick -> manager.itemClick(intention.position)
                is HomeIntention.BackPress -> manager.navigateTo(Destination.Back)
            }.enforce
        }
    }

    val state: LiveData<HomeState> = observeUpnpStateUseCase
        .execute()
        .map { contentState ->
            when (contentState) {
                is ContentState.Loading -> HomeState.Loading
                is ContentState.Success -> HomeState.Success(upnpDirectoryMapper.map(contentState.upnpDirectory))
            }
        }.asLiveData()
}
