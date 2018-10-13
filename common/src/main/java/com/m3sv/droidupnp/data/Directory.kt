package com.m3sv.droidupnp.data


sealed class Directory {
    object Home : Directory()
    data class SubDirectory(val id: String, val parentId: String?) : Directory()
}