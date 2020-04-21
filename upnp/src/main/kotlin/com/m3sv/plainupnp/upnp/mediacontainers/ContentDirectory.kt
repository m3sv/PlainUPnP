package com.m3sv.plainupnp.upnp.mediacontainers

data class ContentDirectory(val name: String) {
    private val numberOfDelimiters: Int = name.count { it == '/' }.plus(2)

    fun samePath(path: String) = path.count { it == '/' } == numberOfDelimiters
}
