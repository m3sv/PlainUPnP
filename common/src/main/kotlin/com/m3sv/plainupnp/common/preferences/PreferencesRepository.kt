package com.m3sv.plainupnp.common.preferences

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.m3sv.plainupnp.ThemeOption
import com.m3sv.plainupnp.applicationmode.ApplicationMode
import com.m3sv.plainupnp.data.upnp.UriWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(private val context: Application) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val Context.preferencesStore: DataStore<Preferences> by dataStore(
        fileName = FILE_NAME,
        serializer = PreferencesSerializer
    )

    private val updateFlow = MutableSharedFlow<Unit>()

    private val persistedUris: MutableStateFlow<List<UriWrapper>> = MutableStateFlow(listOf())

    init {
        scope.launch { updateUris() }
    }

    val preferences: StateFlow<Preferences?> = context
        .preferencesStore
        .data
        .combine(updateFlow) { _, _ -> context.preferencesStore.data.first() }
        .stateIn(
            CoroutineScope(Dispatchers.IO),
            SharingStarted.Eagerly,
            runBlocking { context.preferencesStore.data.first() }
        )

    suspend fun setApplicationMode(applicationMode: ApplicationMode) {
        updatePreferences { builder ->
            val newApplicationMode = when (applicationMode) {
                ApplicationMode.Streaming -> Preferences.ApplicationMode.STREAMING
                ApplicationMode.Player -> Preferences.ApplicationMode.PLAYER
            }

            builder.applicationMode = newApplicationMode
        }
    }

    suspend fun setApplicationTheme(themeOption: ThemeOption) {
        updatePreferences { builder ->
            val newTheme = when (themeOption) {
                ThemeOption.System -> Preferences.Theme.SYSTEM
                ThemeOption.Light -> Preferences.Theme.LIGHT
                ThemeOption.Dark -> Preferences.Theme.DARK
            }

            builder.theme = newTheme
        }
    }

    suspend fun setShareImages(enable: Boolean) {
        updatePreferences { builder ->
            builder.enableImages = enable
        }
    }

    suspend fun setShareVideos(enable: Boolean) {
        updatePreferences { builder ->
            builder.enableVideos = enable
        }
    }

    suspend fun setShareAudio(enable: Boolean) {
        updatePreferences { builder ->
            builder.enableAudio = enable
        }
    }

    suspend fun setShowThumbnails(enable: Boolean) {
        updatePreferences { builder -> builder.enableThumbnails = enable }
    }

    fun persistedUrisFlow(): Flow<List<UriWrapper>> = persistedUris

    fun releaseUri(uriWrapper: UriWrapper) {
        context
            .contentResolver
            .releasePersistableUriPermission(uriWrapper.uriPermission.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        updateUris()
    }

    fun updateUris() {
        scope.launch {
            persistedUris.value = getUris()
            updateFlow.emit(Unit)
        }
    }

    fun getUris(): List<UriWrapper> = context.contentResolver.persistedUriPermissions.map(::UriWrapper)

    private suspend fun updatePreferences(updateFunction: suspend (t: Preferences.Builder) -> Unit) {
        context.preferencesStore.updateData { preferences ->
            preferences.toBuilder().apply { updateFunction(this) }.build()
        }

        updateFlow.emit(Unit)
    }

    companion object {
        private const val FILE_NAME = "preferences.pb"
    }
}
