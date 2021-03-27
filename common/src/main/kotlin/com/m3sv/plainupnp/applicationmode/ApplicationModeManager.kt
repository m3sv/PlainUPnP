package com.m3sv.plainupnp.applicationmode

import android.content.SharedPreferences
import com.m3sv.plainupnp.common.R
import com.m3sv.plainupnp.common.util.StringResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationModeManager @Inject constructor(
    private val stringResolver: StringResolver,
    private val sharedPreferences: SharedPreferences,
) {
    private val preferencesKey by lazy { stringResolver.getString(R.string.key_application_mode) }

    private val modeFlow: MutableSharedFlow<ApplicationMode> = MutableSharedFlow()

    val applicationMode: Flow<ApplicationMode?> = modeFlow.onEach {
        sharedPreferences.edit().apply { putString(preferencesKey, stringResolver.getString(it.stringValue)) }.apply()
    }

    suspend fun setApplicationMode(mode: ApplicationMode) {
        modeFlow.emit(mode)
    }

    fun getApplicationMode(): ApplicationMode? = sharedPreferences
        .getString(preferencesKey, null)?.let { ApplicationMode.byStringValue(stringResolver, it) }
}
