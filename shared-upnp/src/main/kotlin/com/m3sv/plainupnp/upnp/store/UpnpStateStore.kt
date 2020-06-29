package com.m3sv.plainupnp.upnp.store

import kotlinx.coroutines.flow.Flow

interface UpnpStateStore {
    val state: Flow<ContentState>
    fun setState(state: ContentState)
    fun getCurrentState(): ContentState?
}
