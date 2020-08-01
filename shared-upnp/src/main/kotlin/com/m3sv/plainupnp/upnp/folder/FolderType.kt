package com.m3sv.plainupnp.upnp.folder

sealed class FolderType(val folderId: String, val title: String) {
    class Root(folderId: String, title: String) : FolderType(folderId, title)
    class SubFolder(folderId: String, title: String) : FolderType(folderId, title)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FolderType

        if (folderId != other.folderId) return false
        if (title != other.title) return false

        return true
    }

    override fun hashCode(): Int {
        var result = folderId.hashCode()
        result = 31 * result + title.hashCode()
        return result
    }
}
