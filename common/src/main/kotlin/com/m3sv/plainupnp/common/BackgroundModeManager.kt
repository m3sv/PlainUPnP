package com.m3sv.plainupnp.common

import android.content.Context
import androidx.preference.PreferenceManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundModeManager @Inject constructor(private val context: Context) {
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    fun isAllowedToRunInBackground(): Boolean =
        preferences.getBoolean(context.getString(R.string.pref_key_allow_run_in_background), true)
}
