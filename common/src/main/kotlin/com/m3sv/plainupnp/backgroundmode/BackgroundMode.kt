package com.m3sv.plainupnp.backgroundmode

import androidx.annotation.StringRes
import com.m3sv.plainupnp.common.R

enum class BackgroundMode(@StringRes val resourceId: Int) {
    ALLOWED(R.string.run_in_background),
    DENIED(R.string.pause_in_background);

    companion object {

    }
}
