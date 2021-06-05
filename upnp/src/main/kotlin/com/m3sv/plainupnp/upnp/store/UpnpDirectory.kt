package com.m3sv.plainupnp.upnp.store

import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject

sealed class UpnpDirectory(val content: List<ClingDIDLObject>) {
    object None : UpnpDirectory(listOf())

    class Root(
        val name: String,
        content: List<ClingDIDLObject>
    ) : UpnpDirectory(content)

    class SubDirectory(
        val parentName: String,
        content: List<ClingDIDLObject>
    ) : UpnpDirectory(content)
}
