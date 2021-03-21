package com.m3sv.plainupnp.applicationmode

import androidx.annotation.StringRes
import com.m3sv.plainupnp.common.R

enum class ApplicationMode(@StringRes val stringValue: Int) {
    Streaming(R.string.application_mode_streaming),
    Player(R.string.application_mode_player),
}
