package com.m3sv.plainupnp.common.utils

import org.junit.Test

import org.junit.Assert.*

class TimeUtilsKtTest {

    @Test
    fun `format time to proper format valid`() {
        val actual = formatTime(100, 25, 12121)
        val expected = "00:50:30"
        assertEquals(expected, actual)
    }

    @Test
    fun `format time to proper format progress is bigger than max`() {
        val actual = formatTime(100, 250, 12121)
        val expected = null
        assertEquals(expected, actual)
    }

    @Test
    fun `format time to proper format negative input`() {
        val actual = formatTime(-100, -250, -12121)
        val expected = null
        assertEquals(expected, actual)
    }

    @Test
    fun `format time to proper format progress equal to max`() {
        val actual = formatTime(0, 0, 12121)
        val expected = "00:00:00"
        assertEquals(expected, actual)
    }
}