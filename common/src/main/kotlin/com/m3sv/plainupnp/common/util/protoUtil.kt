package com.m3sv.plainupnp.common.util

import com.m3sv.plainupnp.applicationmode.ApplicationMode
import com.m3sv.plainupnp.common.preferences.Preferences

fun Preferences.ApplicationMode.asApplicationMode(): ApplicationMode = when (this) {
    Preferences.ApplicationMode.STREAMING -> ApplicationMode.Streaming
    Preferences.ApplicationMode.PLAYER -> ApplicationMode.Player
    Preferences.ApplicationMode.UNRECOGNIZED -> error("Application mode is uninitialized")
}
