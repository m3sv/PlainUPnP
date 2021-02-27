package com.m3sv.plainupnp.common

import android.app.Application
import androidx.preference.PreferenceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundModeManager @Inject constructor(private val application: Application) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(application)

    fun isAllowedToRunInBackground(): Boolean =
        preferences.getBoolean(application.getString(R.string.pref_key_allow_run_in_background), true)
}
