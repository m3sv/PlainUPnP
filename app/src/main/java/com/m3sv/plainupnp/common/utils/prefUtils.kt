@file:JvmName("PrefUtils")
package com.m3sv.plainupnp.common.utils

import android.content.Context
import android.support.v7.preference.PreferenceManager


const val CONTENT_DIRECTORY_SERVICE = "pref_contentDirectoryService"
const val CONTENT_DIRECTORY_VIDEO = "pref_contentDirectoryService_video"
const val CONTENT_DIRECTORY_AUDIO = "pref_contentDirectoryService_audio"
const val CONTENT_DIRECTORY_IMAGE = "pref_contentDirectoryService_image"
const val CONTENT_DIRECTORY_NAME = "pref_contentDirectoryService_name"

fun getSettingContentDirectoryName(ctx: Context): String {
    val value = PreferenceManager.getDefaultSharedPreferences(ctx)
        .getString(CONTENT_DIRECTORY_NAME, "")
    return if (value != "") value else android.os.Build.MODEL
}