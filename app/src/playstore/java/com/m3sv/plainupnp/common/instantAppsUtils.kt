package com.m3sv.plainupnp.common

import android.content.Context
import com.google.android.instantapps.InstantApps

fun isInstantApp(context: Context) = InstantApps.isInstantApp(context)