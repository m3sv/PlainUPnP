package com.m3sv.plainupnp.upnp.resourceproviders

import android.content.Context
import android.content.pm.PackageManager
import com.m3sv.plainupnp.upnp.R
import timber.log.Timber
import javax.inject.Inject

class LocalServiceResourceProvider @Inject constructor(private val context: Context) {
    val appName: String = context.getString(R.string.app_name)
    val appUrl: String = context.getString(R.string.app_url)
    val settingContentDirectoryName: String = android.os.Build.MODEL

    // TODO have different model number for different flavors
    val modelNumber = context.getString(R.string.model_number)
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
