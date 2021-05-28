package com.m3sv.plainupnp.common.util

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
val isQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
