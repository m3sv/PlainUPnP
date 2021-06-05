package com.m3sv.plainupnp.upnp

import kotlinx.coroutines.CoroutineScope

interface UpnpScopeProvider {
    val upnpScope: CoroutineScope
}
