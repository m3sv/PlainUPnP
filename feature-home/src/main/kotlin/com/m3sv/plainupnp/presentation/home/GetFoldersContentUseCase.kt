package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.upnp.didl.ClingDIDLContainer
import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import javax.inject.Inject

data class FolderContents(
    val contents: Folder,
    val folders: List<ClingDIDLContainer>,
    val media: List<ClingDIDLObject>,
)

class GetFolderContentUseCase @Inject constructor(
    private val manager: UpnpManager,
    private val clingContentMapper: ClingContentMapper,
) {
    fun get(): FolderContents {
        val folderName = manager.getCurrentFolderName()
        val folderContents = manager.getCurrentFolderContents()

        val folders = folderContents.filterIsInstance<ClingDIDLContainer>()
        val media = folderContents.filter { it !is ClingDIDLContainer }
        val contents = Folder(
            name = folderName,
            contents = clingContentMapper.map(folderContents)
        )

        return FolderContents(contents, folders, media)
    }
}
