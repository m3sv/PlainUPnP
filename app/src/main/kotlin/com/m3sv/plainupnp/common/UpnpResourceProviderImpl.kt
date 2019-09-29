package com.m3sv.plainupnp.common

import android.content.Context
import com.m3sv.plainupnp.upnp.UpnpResourceProvider
import javax.inject.Inject

class UpnpResourceProviderImpl @Inject constructor(context: Context) : UpnpResourceProvider {
    override val playLocally: String = context.getString(R.string.play_locally)
}