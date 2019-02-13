@file:JvmName("PrefUtils")

package com.m3sv.plainupnp.common.utils

import android.content.Context
import androidx.preference.PreferenceManager


const val CONTENT_DIRECTORY_SERVICE = "pref_contentDirectoryService"
const val CONTENT_DIRECTORY_VIDEO = "pref_contentDirectoryService_video"
const val CONTENT_DIRECTORY_AUDIO = "pref_contentDirectoryService_audio"
const val CONTENT_DIRECTORY_IMAGE = "pref_contentDirectoryService_image"
const val CONTENT_DIRECTORY_NAME = "pref_contentDirectoryService_name"

fun getSettingContentDirectoryName(context: Context): String =
    PreferenceManager.getDefaultSharedPreferences(context)
        .getString(CONTENT_DIRECTORY_NAME, android.os.Build.MODEL) ?: android.os.Build.MODEL