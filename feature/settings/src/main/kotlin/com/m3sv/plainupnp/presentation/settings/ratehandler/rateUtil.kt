package com.m3sv.plainupnp.presentation.settings.ratehandler

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.m3sv.plainupnp.presentation.settings.R

internal fun Activity.openPlayStore() =
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("${getString(R.string.market_prefix)}$packageName")))

internal fun Activity.openPlayStoreFallback() {
    Intent(Intent.ACTION_VIEW, Uri.parse("${getString(R.string.play_prefix)}$packageName")).also(::startActivity)
}
