package com.m3sv.plainupnp.applicationmode

import android.content.Context
import androidx.annotation.StringRes
import com.m3sv.plainupnp.common.R

enum class ApplicationMode(@StringRes val stringValue: Int) {
    Streaming(R.string.application_mode_streaming),
    Player(R.string.application_mode_player);

    companion object {
        fun byStringValue(context: Context, value: String) =
            values().firstOrNull { context.getString(it.stringValue) == value }
    }
}
