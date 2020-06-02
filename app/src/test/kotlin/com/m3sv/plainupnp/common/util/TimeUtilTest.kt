package com.m3sv.plainupnp.common.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class TimeUtilTest {

    @Test
    fun `format time to proper format valid`() {
        val actual = formatTime(100, 25, 12121)
        val expected = "00:50:30"
        assertEquals(expected, actual)
    }

    @Test
    fun `format time to proper format progress is bigger than max`() {
        val actual = formatTime(100, 250, 12121)
        assertEquals(DEFAULT_DURATION, actual)
    }

    @Test
    fun `format time to proper format negative input`() {
        val actual = formatTime(-100, -250, -12121)
        assertEquals(DEFAULT_DURATION, actual)
    }

    @Test
    fun `format time to proper format progress equal to max`() {
        val actual = formatTime(0, 0, 12121)
        assertEquals(DEFAULT_DURATION, actual)
    }

    companion object {
        const val DEFAULT_DURATION = "00:00:00"
    }
}
