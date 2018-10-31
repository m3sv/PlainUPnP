package org.droidupnp.legacy.upnp

import com.m3sv.plainupnp.data.upnp.DIDLObjectDisplay

typealias ContentCallback = (List<DIDLObjectDisplay>?) -> Unit

interface IContentDirectoryCommand {
    fun browse(directoryID: String, parent: String?, callback: ContentCallback)

    fun search(search: String, parent: String?, callback: ContentCallback)
}
