package com.m3sv.plainupnp.upnp.folder

import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject

sealed class Folder(
    val id: String,
    val title: String,
    val contents: List<ClingDIDLObject>,
) {
    class Root(
        id: String,
        title: String,
        contents: List<ClingDIDLObject>,
    ) : Folder(id, title, contents)

    class SubFolder(
        id: String,
        title: String,
        contents: List<ClingDIDLObject>,
    ) : Folder(id, title, contents)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Folder

        if (id != other.id) return false
        if (title != other.title) return false
        if (contents != other.contents) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + contents.hashCode()
        return result
    }

}
