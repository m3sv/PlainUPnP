package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.upnp.folder.Folder
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class FolderManager @Inject constructor(private val upnpManager: UpnpManager) {

    private var fileStructure: Deque<Folder> = LinkedList()

    private val fileStructureFlow: MutableSharedFlow<List<Folder>> = MutableSharedFlow()

    fun observe(): Flow<List<Folder>> =
        merge(upnpManager
            .folderChangeFlow
            .map { folder ->
                when (folder) {
                    is Folder.Root -> {
                        with(fileStructure) {
                            clear()
                            add(folder)
                        }
                    }
                    is Folder.SubFolder -> fileStructure.add(folder)
                }

                fileStructure.toList()
            },
            fileStructureFlow)

    private suspend fun navigateBack() {
        fileStructure.pollLast()
        updateClients()
    }

    suspend fun backTo(folder: Folder?) {
        if (folder == null) {
            navigateBack()
            return
        }

        val fromIndex = fileStructure.indexOf(folder)
        if (fromIndex == -1) {
            Timber.d("Didn't find folder $folder")
            return
        }

        fileStructure = LinkedList(LinkedList(fileStructure).subList(0, fromIndex + 1))
        updateClients()
    }

    private suspend fun updateClients() {
        fileStructureFlow.emit(fileStructure.toList())
    }

}
