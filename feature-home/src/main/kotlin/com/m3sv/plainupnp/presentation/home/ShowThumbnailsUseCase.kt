package com.m3sv.plainupnp.presentation.home

import android.content.Context
import android.content.SharedPreferences
import com.m3sv.plainupnp.common.R
import javax.inject.Inject

class ShowThumbnailsUseCase @Inject constructor(
    context: Context,
    private val preferences: SharedPreferences
) {
    private val key = context.getString(R.string.enable_thumbnails_key)

    operator fun invoke() = preferences.getBoolean(key, false)
}
