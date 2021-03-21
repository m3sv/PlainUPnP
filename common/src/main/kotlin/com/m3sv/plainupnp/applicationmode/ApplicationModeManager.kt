package com.m3sv.plainupnp.applicationmode

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationModeManager @Inject constructor(private val sharedPreferences: SharedPreferences) {

    var applicationMode: ApplicationMode = getSavedApplicationMode()
        get() = getSavedApplicationMode()
        set(value) {
            field = value
            sharedPreferences.edit().apply {
                putInt(APP_MODE_KEY, ApplicationMode.values().indexOf(value))
            }.apply()
        }

    private fun getSavedApplicationMode(): ApplicationMode =
        ApplicationMode.values()[sharedPreferences.getInt(APP_MODE_KEY, 0)]

    companion object {
        private const val APP_MODE_KEY = "app_mode_key"
    }
}
