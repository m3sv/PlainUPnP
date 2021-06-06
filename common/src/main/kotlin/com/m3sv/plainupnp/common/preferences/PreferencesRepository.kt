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
import org.fourthline.cling.model.types.UDN
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(private val context: Application) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val Context.preferencesStore: DataStore<Preferences> by dataStore(
        fileName = FILE_NAME,
        serializer = PreferencesSerializer
    )

    private val _updateFlow = MutableSharedFlow<Boolean>()

    private val persistedUris: MutableStateFlow<List<UriWrapper>> = MutableStateFlow(listOf())

    val updateFlow: Flow<Boolean> = _updateFlow

    val preferences: StateFlow<Preferences> = context
        .preferencesStore
        .data
        .stateIn(
            CoroutineScope(Dispatchers.IO),
            SharingStarted.Eagerly,
            runBlocking { context.preferencesStore.data.first() }
        )

    val theme: StateFlow<ThemeOption> = preferences.map { preferences ->
        when (preferences.theme) {
            Preferences.Theme.LIGHT -> ThemeOption.Light
            Preferences.Theme.DARK -> ThemeOption.Dark
            Preferences.Theme.SYSTEM,
            Preferences.Theme.UNRECOGNIZED -> ThemeOption.System
        }
    }.stateIn(scope, SharingStarted.Lazily, ThemeOption.System)

    init {
        scope.launch {
            if (preferences.value.udn == null) {
                initUdn()
            }

            updateUris()
        }
    }

    val pauseInBackground: Boolean
        get() = preferences.value.pauseInBackground

    suspend fun setApplicationMode(applicationMode: ApplicationMode) {
        updatePreferences(true) { builder ->
            val newApplicationMode = when (applicationMode) {
                ApplicationMode.Streaming -> Preferences.ApplicationMode.STREAMING
                ApplicationMode.Player -> Preferences.ApplicationMode.PLAYER
            }

            builder.applicationMode = newApplicationMode
        }
    }

    suspend fun setApplicationTheme(themeOption: ThemeOption) {
        updatePreferences(false) { builder ->
            val newTheme = when (themeOption) {
                ThemeOption.System -> Preferences.Theme.SYSTEM
                ThemeOption.Light -> Preferences.Theme.LIGHT
                ThemeOption.Dark -> Preferences.Theme.DARK
            }

            builder.theme = newTheme
        }
    }

    suspend fun setShareImages(enable: Boolean) {
        updatePreferences(true) { builder ->
            builder.enableImages = enable
        }
    }

    suspend fun setShareVideos(enable: Boolean) {
        updatePreferences(true) { builder ->
            builder.enableVideos = enable
        }
    }

    suspend fun setShareAudio(enable: Boolean) {
        updatePreferences(true) { builder ->
            builder.enableAudio = enable
        }
    }

    suspend fun setShowThumbnails(enable: Boolean) {
        updatePreferences(false) { builder -> builder.enableThumbnails = enable }
    }

    suspend fun finishOnboarding() {
        updatePreferences(false) { builder -> builder.finishedOnboarding = true }
    }

    suspend fun setPauseInBackground(pause: Boolean) {
        updatePreferences(false) { builder -> builder.pauseInBackground = pause }
    }

    private suspend fun initUdn() {
        updatePreferences(false) { builder -> builder.udn = UDN(UUID.randomUUID()).toString() }
    }

    fun getUdn(): UDN = UDN.valueOf(preferences.value.udn)

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
            _updateFlow.emit(true)
        }
    }

    fun getUris(): List<UriWrapper> = context.contentResolver.persistedUriPermissions.map(::UriWrapper)

    private suspend inline fun updatePreferences(
        refreshContent: Boolean,
        crossinline updateFunction: (t: Preferences.Builder) -> Unit,
    ) {
        context.preferencesStore.updateData { preferences ->
            preferences.toBuilder().apply { updateFunction(this) }.build()
        }

        _updateFlow.emit(refreshContent)
    }

    companion object {
        private const val FILE_NAME = "preferences.pb"
    }
}
