package com.m3sv.plainupnp.upnp.usecase

import com.m3sv.plainupnp.upnp.ContentState
import com.m3sv.plainupnp.upnp.UpnpStateStore
import io.reactivex.Observable
import javax.inject.Inject

class ObserveUpnpStateUseCase @Inject constructor(private val upnpStateStore: UpnpStateStore) {
    fun execute(): Observable<ContentState> = upnpStateStore.state
}
