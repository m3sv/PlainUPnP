package org.droidupnp.legacy.upnp.didl

import com.m3sv.plainupnp.upnp.IDIDLObject

interface IDIDLContainer : IDIDLObject {
    val childCount: Int
}
