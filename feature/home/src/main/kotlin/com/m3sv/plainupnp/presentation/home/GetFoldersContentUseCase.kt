package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.upnp.manager.UpnpManager
import javax.inject.Inject

class GetFolderContentUseCase @Inject constructor(
    private val manager: UpnpManager,
    private val clingContentMapper: ClingContentMapper,
) {
    fun get(): Folder {
        val folderName = manager.getCurrentFolderName()
        val folderContents = manager.getCurrentFolderContents()

        return Folder(
            name = folderName,
            contents = clingContentMapper.map(folderContents).sortedBy { it.type }
        )
    }
}
