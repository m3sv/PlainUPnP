package com.m3sv.plainupnp.common.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import com.m3sv.plainupnp.ThemeOption
import com.m3sv.plainupnp.applicationmode.ApplicationMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(private val context: Context) {

    private val Context.preferencesStore: DataStore<Preferences> by dataStore(
        fileName = FILE_NAME,
        serializer = PreferencesSerializer
    )

    val preferences: Flow<Preferences> = context.preferencesStore.data

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

    suspend fun updateImageContainerStatus(enable: Boolean) {
        updatePreferences { builder ->
            builder.enableImages = enable
        }
    }

    suspend fun updateVideoContainerStatus(enable: Boolean) {
        updatePreferences { builder ->
            builder.enableVideos = enable
        }
    }

    suspend fun updateAudioContainerStatus(enable: Boolean) {
        updatePreferences { builder ->
            builder.enableAudio = enable
        }
    }

    suspend fun updateThumbnailsStatus(enable: Boolean) {
        updatePreferences { builder -> builder.enableThumbnails = enable }
    }

    private suspend fun updatePreferences(updateFunction: suspend (t: Preferences.Builder) -> Unit) {
        context.preferencesStore.updateData { preferences ->
            preferences.toBuilder().apply { updateFunction(this) }.build()
        }
    }

    companion object {
        private const val FILE_NAME = "preferences.pb"
    }
}
