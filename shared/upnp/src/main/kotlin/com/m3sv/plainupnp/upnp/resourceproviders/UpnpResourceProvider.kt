package com.m3sv.plainupnp.upnp.resourceproviders

import android.app.Application
import com.m3sv.plainupnp.common.R
import javax.inject.Inject

class UpnpResourceProvider @Inject constructor(context: Application) {
    val playLocally: String = context.getString(R.string.play_locally)
}
