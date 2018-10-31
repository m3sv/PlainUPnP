package com.m3sv.plainupnp.data.upnp


sealed class Directory {
    object Home : Directory()
    data class SubDirectory(val id: String, val parentId: String?) : Directory()
}