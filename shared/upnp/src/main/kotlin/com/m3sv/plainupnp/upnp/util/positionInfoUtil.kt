package com.m3sv.plainupnp.upnp.util

import org.fourthline.cling.support.model.PositionInfo
import java.time.Duration
import kotlin.math.abs

inline val PositionInfo.remainingDuration: String
    get() {
        val duration = Duration.ofSeconds(trackRemainingSeconds)
        val seconds = duration.seconds
        val absSeconds = abs(seconds)
        val positive = "%d:%02d:%02d".format(
            absSeconds / 3600,
            (absSeconds % 3600) / 60,
            absSeconds % 60
        )

        val sign = if (seconds < 0) "-" else ""

        return "$sign$positive"
    }

inline val PositionInfo.duration: String
    get() {
        val duration = Duration.ofSeconds(trackDurationSeconds)
        val seconds = duration.seconds
        val absSeconds = abs(seconds)
        val positive = "%d:%02d:%02d".format(
            absSeconds / 3600,
            (absSeconds % 3600) / 60,
            absSeconds % 60
        )

        val sign = if (seconds < 0) "-" else ""

        return "$sign$positive"
    }

inline val PositionInfo.position: String
    get() {
        val duration = Duration.ofSeconds(trackElapsedSeconds)
        val seconds = duration.seconds
        val absSeconds = abs(seconds)
        val positive = "%d:%02d:%02d".format(
            absSeconds / 3600,
            (absSeconds % 3600) / 60,
            absSeconds % 60
        )

        val sign = if (seconds < 0) "-" else ""

        return "$sign$positive"
    }
