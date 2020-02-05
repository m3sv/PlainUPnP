package com.m3sv.plainupnp.common

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class AreThumbnailsEnabledUseCase @Inject constructor(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) {
    operator fun invoke(): Boolean = sharedPreferences.getBoolean(
        context.getString(R.string.enable_thumbnails_key),
        true
    )
}
