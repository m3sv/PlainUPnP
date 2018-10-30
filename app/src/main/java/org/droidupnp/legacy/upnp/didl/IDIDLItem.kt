package org.droidupnp.legacy.upnp.didl

import com.m3sv.plainupnp.upnp.IDIDLObject

interface IDIDLItem : IDIDLObject {
    val uri: String?
}
