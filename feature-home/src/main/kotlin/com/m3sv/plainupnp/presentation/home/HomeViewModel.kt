package com.m3sv.plainupnp.presentation.home

import androidx.lifecycle.*
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.core.eventbus.post
import com.m3sv.plainupnp.upnp.didl.ClingDIDLContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.manager.PlayItem
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.m3sv.plainupnp.upnp.folder.Folder as UpnpFolder

data class Folder(val name: String, val contents: List<ContentItem>)

class HomeViewModel @Inject constructor(
    private val getFolderContentUseCase: GetFolderContentUseCase,
    filterDelegate: FilterDelegate,
) : ViewModel() {

    private val _currentFolderContents = MutableLiveData<Folder>()

    private lateinit var folders: List<ClingDIDLContainer>
    private lateinit var media: List<ClingDIDLObject>

    val currentFolderContents: LiveData<Folder> = _currentFolderContents

    // TODO Filtering must be done in a separate use case, refactor this
    val filterText: LiveData<String> = filterDelegate
        .state
        .asLiveData()

    fun itemClick(clickPosition: Int) {
        viewModelScope.launch {
            when {
                folders.isEmpty() && media.isEmpty() -> return@launch
                folders.isEmpty() -> post(
                    MediaItemClick(PlayItem(
                        media[clickPosition],
                        media.listIterator(clickPosition))))
                else -> handleFolderOrMediaClick(clickPosition)
            }
        }
    }

    private fun handleFolderOrMediaClick(clickPosition: Int) {
        when {
            // we're in the media zone
            clickPosition >= folders.size -> {
                val mediaPosition = clickPosition - folders.size
                val mediaItem = media[mediaPosition]

                post(MediaItemClick(PlayItem(
                    mediaItem,
                    media.listIterator(mediaPosition)))
                )
            }
            // we're in the folder zone
            else -> {
                val folder = folders[clickPosition]
                post(FolderClick(UpnpFolder.SubFolder(folder.id, folder.title)))
            }
        }
    }

    fun refreshFolderContents() {
        viewModelScope.launch {
            val folderContents = getFolderContentUseCase.get()
            media = folderContents.media
            folders = folderContents.folders
            _currentFolderContents.postValue(folderContents.contents)
        }
    }
}
