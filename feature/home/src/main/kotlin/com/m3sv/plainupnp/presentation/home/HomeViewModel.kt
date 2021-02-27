package com.m3sv.plainupnp.presentation.home

import androidx.lifecycle.*
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.core.eventbus.post
import com.m3sv.plainupnp.upnp.didl.ClingContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.didl.ClingMedia
import com.m3sv.plainupnp.upnp.manager.PlayItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.m3sv.plainupnp.upnp.folder.Folder as UpnpFolder

data class Folder(val name: String, val contents: List<ContentItem>)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFolderContentUseCase: GetFolderContentUseCase,
    filterDelegate: FilterDelegate,
) : ViewModel() {

    private val _currentFolderContents = MutableLiveData<List<ContentItem>>()
    private var clingContents: List<ClingDIDLObject> = listOf()

    private var folders: List<ClingContainer> = listOf()
    private var media: List<ClingMedia> = listOf()

    val currentFolderContents: LiveData<List<ContentItem>> = _currentFolderContents

    val filterText: LiveData<String> = filterDelegate
        .state
        .asLiveData()

    fun itemClick(clickPosition: Int) {
        viewModelScope.launch {
            handleClick(clickPosition, false)
        }
    }

    fun itemLongClick(clickPosition: Int) {
        viewModelScope.launch {
            handleClick(clickPosition, true)
        }
    }

    private fun handleClick(clickPosition: Int, isLongClick: Boolean) {
        _currentFolderContents.value?.let { contents ->
            val item = contents[clickPosition]

            when (item.type) {
                ContentType.FOLDER,
                ContentType.USER_SELECTED_FOLDER,
                -> post(FolderClick(UpnpFolder.SubFolder(item.clingItem.id, item.clingItem.title)))
                else -> {
                    val event = if (isLongClick) {
                        MediaItemLongClick(PlayItem(
                            item.clingItem,
                            media.listIterator(clickPosition - folders.size)))
                    } else {
                        MediaItemClick(PlayItem(
                            item.clingItem,
                            media.listIterator(clickPosition - folders.size)))
                    }
                    post(event)
                }
            }
        }
    }

    fun refreshFolderContents() {
        viewModelScope.launch {
            val folderContents = getFolderContentUseCase.get()
            clingContents = folderContents.contents.map { it.clingItem }
            folders = clingContents.filterIsInstance<ClingContainer>()
            media = clingContents.filterIsInstance<ClingMedia>()
            _currentFolderContents.postValue(folderContents.contents)
        }
    }
}
