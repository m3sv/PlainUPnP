package com.m3sv.plainupnp.backgroundmode

import android.content.SharedPreferences
import androidx.core.content.edit
import com.m3sv.plainupnp.common.R
import com.m3sv.plainupnp.common.util.StringResolver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackgroundModeManager @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val stringResolver: StringResolver,
) : StringResolver by stringResolver {

    var backgroundMode: BackgroundMode = BackgroundMode.ALLOWED
        get() = sharedPreferences.getBoolean(getString(R.string.pref_key_allow_run_in_background), true)
            .let { allowed -> if (allowed) BackgroundMode.ALLOWED else BackgroundMode.DENIED }
        set(value) {
            sharedPreferences.edit {
                putBoolean(getString(R.string.pref_key_allow_run_in_background), value == BackgroundMode.ALLOWED)
            }
            field = value
        }
}
