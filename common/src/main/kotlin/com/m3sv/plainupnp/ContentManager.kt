package com.m3sv.plainupnp

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.content.edit
import com.m3sv.plainupnp.common.R
import com.m3sv.plainupnp.common.util.StringResolver
import com.m3sv.plainupnp.data.upnp.UriWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class ContentManager @Inject constructor(
    private val application: Application,
    private val sharedPreferences: SharedPreferences,
    private val stringResolver: StringResolver,
) : CoroutineScope, StringResolver by stringResolver {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    private val persistedUris: MutableStateFlow<List<UriWrapper>> = MutableStateFlow(listOf())

    private val defaultContainerValue = application.resources.getBoolean(R.bool.default_enable_content_directory)

    var isImagesEnabled = false
        get() = R.string.pref_enable_image_container_key.getContainerValue()
        set(value) {
            field = value

            R.string.pref_enable_image_container_key.setContainerValue(value)
        }

    var isAudioEnabled = false
        get() = R.string.pref_enable_audio_container_key.getContainerValue()
        set(value) {
            field = value

            R.string.pref_enable_audio_container_key.setContainerValue(value)
        }

    var isVideoEnabled = false
        get() = R.string.pref_enable_video_container_key.getContainerValue()
        set(value) {
            field = value

            R.string.pref_enable_video_container_key.setContainerValue(value)
        }

    private fun Int.getContainerValue(): Boolean =
        sharedPreferences.getBoolean(getString(this), defaultContainerValue)

    private fun Int.setContainerValue(value: Boolean) = sharedPreferences.edit {
        putBoolean(application.getString(this@setContainerValue), value)
    }

    private val _refreshFlow = MutableSharedFlow<Unit>()

    val refreshFlow: Flow<Unit> = _refreshFlow

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
            _refreshFlow.emit(Unit)
        }
    }

    fun getUris(): List<UriWrapper> = application.contentResolver.persistedUriPermissions.map(::UriWrapper)
}
