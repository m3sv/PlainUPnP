package com.m3sv.plainupnp.applicationmode

import android.app.Application
import android.content.SharedPreferences
import com.m3sv.plainupnp.common.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationModeManager @Inject constructor(
    private val application: Application,
    private val sharedPreferences: SharedPreferences,
) {
    private val preferencesKey by lazy { application.getString(R.string.key_application_mode) }

    var applicationMode: ApplicationMode = getSavedApplicationMode()
        get() = getSavedApplicationMode()
        set(value) {
            field = value
            sharedPreferences.edit().apply {
                putString(preferencesKey, application.getString(value.stringValue))
            }.apply()
        }

    private fun getSavedApplicationMode(): ApplicationMode = sharedPreferences
        .getString(preferencesKey, null)
        ?.let { ApplicationMode.byStringValue(application, it) }
        ?: ApplicationMode.Streaming

}
