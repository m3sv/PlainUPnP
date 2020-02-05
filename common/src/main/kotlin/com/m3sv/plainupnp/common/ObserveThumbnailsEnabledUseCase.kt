package com.m3sv.plainupnp.common

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class ObserveThumbnailsEnabledUseCase @Inject constructor(
    private val context: Context,
    private val areThumbnailsEnabled: AreThumbnailsEnabledUseCase
) {

    operator fun invoke(): Flow<Boolean> = callbackFlow {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                offer(areThumbnailsEnabled())
            }

        offer(areThumbnailsEnabled())

        preferences.registerOnSharedPreferenceChangeListener(listener)

        awaitClose {
            preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
}
