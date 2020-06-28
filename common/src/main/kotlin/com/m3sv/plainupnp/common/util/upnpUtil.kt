package com.m3sv.plainupnp.common.util

import android.content.Context
import androidx.preference.PreferenceManager
import org.fourthline.cling.model.types.UDN
import java.util.*

const val UDN_KEY = "local_uuid_key"
fun Context.generateUdn() {
    if (getUdn() == null) {
        val udn = UDN(UUID.randomUUID())
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .edit()
            .putString(UDN_KEY, udn.toString())
            .apply()
    }
}

fun Context.getUdn(): UDN? {
    val udnString = PreferenceManager
        .getDefaultSharedPreferences(this)
        .getString(UDN_KEY, null)
        ?: return null

    return UDN.valueOf(udnString)
}
