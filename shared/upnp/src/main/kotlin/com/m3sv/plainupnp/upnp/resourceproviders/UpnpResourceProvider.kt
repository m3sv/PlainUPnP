package com.m3sv.plainupnp.upnp.resourceproviders

import android.content.Context
import com.m3sv.plainupnp.common.R
import javax.inject.Inject

class UpnpResourceProvider @Inject constructor(context: Context) {
    val playLocally: String = context.getString(R.string.play_locally)
}