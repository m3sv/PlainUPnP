package com.m3sv.plainupnp.presentation.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.common.Mapper
import com.m3sv.plainupnp.upnp.Destination
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import com.m3sv.plainupnp.upnp.store.ContentState
import com.m3sv.plainupnp.upnp.store.UpnpDirectory
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
) : ViewModel() {

    // TODO Filtering must be done in a separate use case, refactor this
    val filterText: LiveData<String> = filterDelegate
        .state
        .asLiveData()

    fun itemClick(position: Int) {
        viewModelScope.launch {
            manager.itemClick(position)
        }
    }

    fun backPress() {
        manager.navigateTo(Destination.Back, null, null)
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
