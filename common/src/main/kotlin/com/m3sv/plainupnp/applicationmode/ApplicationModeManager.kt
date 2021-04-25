package com.m3sv.plainupnp.applicationmode

import android.content.SharedPreferences
import com.m3sv.plainupnp.common.R
import com.m3sv.plainupnp.common.util.StringResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationModeManager @Inject constructor(
    private val stringResolver: StringResolver,
    private val sharedPreferences: SharedPreferences,
) {
    private val preferencesKey by lazy { stringResolver.getString(R.string.key_set_application_mode) }

    private val modeFlow: MutableStateFlow<ApplicationMode?> = MutableStateFlow(getApplicationMode())

    val applicationMode: StateFlow<ApplicationMode?> = modeFlow

    suspend fun setApplicationMode(mode: ApplicationMode) {
        sharedPreferences.edit().apply { putString(preferencesKey, stringResolver.getString(mode.stringValue)) }.apply()
        modeFlow.emit(mode)
    }

    suspend fun setApplicationMode(mode: String) {
        val mode = ApplicationMode.byStringValue(stringResolver, mode) ?: return
        setApplicationMode(mode)
    }

    fun getApplicationMode(): ApplicationMode = sharedPreferences.getString(preferencesKey, null)
        ?.let { ApplicationMode.byStringValue(stringResolver, it) }
        ?: ApplicationMode.Streaming
}
