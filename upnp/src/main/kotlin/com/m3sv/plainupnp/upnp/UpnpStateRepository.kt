package com.m3sv.plainupnp.upnp

import com.m3sv.plainupnp.data.upnp.DIDLObjectDisplay
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

interface UpnpStateStore {

    val state: Observable<ContentState>

    suspend fun setState(state: ContentState)

    suspend fun peekState(): ContentState?
}

sealed class UpnpDirectory(val content: List<DIDLObjectDisplay>) {
    class Root(
        val name: String,
        content: List<DIDLObjectDisplay>
    ) : UpnpDirectory(content)

    class SubUpnpDirectory(
        val parentName: String,
        content: List<DIDLObjectDisplay>
    ) : UpnpDirectory(content)
}

sealed class ContentState {
    object Loading : ContentState()
    data class Success(val upnpDirectory: UpnpDirectory) : ContentState()
}

class UpnpStateRepository @Inject constructor() : UpnpStateStore {

    private val contentSubject = PublishSubject.create<ContentState>()

    private var currentState: ContentState? = null

    override val state: Observable<ContentState> = contentSubject

    override suspend fun setState(state: ContentState) {
        currentState = state
        contentSubject.onNext(state)
    }

    override suspend fun peekState(): ContentState? = currentState
}
