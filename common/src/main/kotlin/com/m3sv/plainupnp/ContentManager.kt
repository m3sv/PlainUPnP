package com.m3sv.plainupnp

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import com.m3sv.plainupnp.data.upnp.UriWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class ContentManager @Inject constructor(
    private val application: Application,
    private val sharedPreferences: SharedPreferences,
    private val contentRepository: ContentRepository,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    private val persistedUris: MutableStateFlow<List<UriWrapper>> = MutableStateFlow(listOf())

    init {
        updateUris()
    }

    fun persistedUrisFlow(): Flow<List<UriWrapper>> = persistedUris

    fun releaseUri(uriWrapper: UriWrapper) {
        launch {
            application
                .contentResolver
                .releasePersistableUriPermission(uriWrapper.uriPermission.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            updateUris()
        }
    }

    fun updateUris() {
        launch {
            persistedUris.value = getUris()
            contentRepository.refreshContent()
        }
    }

    fun getUris(): List<UriWrapper> = application.contentResolver.persistedUriPermissions.map(::UriWrapper)

    // TODO Add selection of common types(image/audio/video)
}
