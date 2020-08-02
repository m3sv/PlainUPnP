package com.m3sv.plainupnp.presentation.home

import androidx.lifecycle.*
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.upnp.didl.ClingDIDLContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import kotlinx.coroutines.launch
import javax.inject.Inject

data class Folder(val name: String, val contents: List<ContentItem>)

class HomeViewModel @Inject constructor(
    private val manager: UpnpManager,
    private val clingContentMapper: ClingContentMapper,
    filterDelegate: FilterDelegate
) : ViewModel() {

    private val _currentFolderContents = MutableLiveData<Folder>()

    val currentFolderContents: LiveData<Folder> = _currentFolderContents

    private var folderName: String = ""

    private var folders: List<ClingDIDLContainer> = listOf()

    private var media: List<ClingDIDLObject> = listOf()

    // TODO Filtering must be done in a separate use case, refactor this
    val filterText: LiveData<String> = filterDelegate
        .state
        .asLiveData()

    fun itemClick(clickPosition: Int) {
        viewModelScope.launch {
            when {
                folders.isEmpty() && media.isEmpty() -> return@launch
                folders.isEmpty() -> manager.playItem(
                    media[clickPosition],
                    media.listIterator(clickPosition)
                )
                else -> handleFolderOrMediaClick(clickPosition)
            }
        }
    }

    private fun handleFolderOrMediaClick(clickPosition: Int) {
        when {
            // we're in the media zone
            clickPosition > folders.size -> {
                val mediaPosition = clickPosition - folders.size
                val mediaItem = media[mediaPosition]
                manager.playItem(mediaItem, media.listIterator(mediaPosition))
            }
            // we're in the folder zone
            else -> {
                val folder = folders[clickPosition]
                manager.navigateTo(
                    folderId = folder.id,
                    title = folder.title
                )
            }
        }
    }

    fun requestCurrentFolderContents() {
        folderName = manager.getCurrentFolderName()
        val folderContents = manager.getCurrentFolderContents()

        folders = folderContents.filterIsInstance<ClingDIDLContainer>()
        media = folderContents.filter { it !is ClingDIDLContainer }

        val folder = Folder(
            name = folderName,
            contents = clingContentMapper.map(folderContents)
        )

        _currentFolderContents.postValue(folder)
    }
}
