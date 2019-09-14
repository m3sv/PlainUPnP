package com.m3sv.plainupnp.upnp.cleanslate

import android.content.Context
import android.content.pm.PackageManager
import com.m3sv.plainupnp.upnp.R
import com.m3sv.plainupnp.upnp.getSettingContentDirectoryName
import timber.log.Timber
import javax.inject.Inject


class LocalServiceResourceProvider @Inject constructor(private val context: Context) {
    val appName: String = context.getString(R.string.app_name)
    val appUrl: String = context.getString(R.string.app_url)
    val settingContentDirectoryName = getSettingContentDirectoryName(context)
    val appVersion: String
        get() {
            var result = "1.0"
            try {
                result = context.packageManager.getPackageInfo(context.packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.e(e, "Application version name not found")
            }
            return result
        }
}
