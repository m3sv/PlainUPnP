package com.m3sv.plainupnp.upnp.resourceproviders

import android.app.Application
import android.content.pm.PackageManager
import com.m3sv.plainupnp.upnp.R
import timber.log.Timber
import javax.inject.Inject

class LocalServiceResourceProvider @Inject constructor(private val application: Application) {
    val appName: String = application.getString(R.string.app_name)
    val appUrl: String = application.getString(R.string.app_url)
    val settingContentDirectoryName: String = android.os.Build.MODEL

    // TODO have different model number for different flavors
    val modelNumber = application.getString(R.string.model_number)
    val appVersion: String
        get() {
            var result = "1.0"
            try {
                result = application.packageManager.getPackageInfo(application.packageName, 0).versionName
            } catch (e: PackageManager.NameNotFoundException) {
                Timber.e(e, "Application version name not found")
            }
            return result
        }
}
