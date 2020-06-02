package com.m3sv.plainupnp.common.util

/**
 * @param duration in seconds
 */
fun formatTime(max: Int, progress: Int, duration: Long): String {
    if (progress < 0 || max < 0 || duration < 0) {
        return "00:00:00"
    }

    if (progress > max) {
        return "00:00:00"
    }

    fun formatTime(h: Long, m: Long, s: Long): String {
        return ((if (h >= 10) "" + h else "0$h") + ":" + (if (m >= 10) "" + m else "0$m") + ":"
                + if (s >= 10) "" + s else "0$s")
    }

    val t = ((1.0 - (max - progress) / max.toDouble()) * duration).toLong()
    val h = t / 3600
    val m = (t - h * 3600) / 60
    val s = t - h * 3600 - m * 60

    return formatTime(h, m, s)
}
