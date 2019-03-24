package com.m3sv.plainupnp.data.upnp


sealed class Directory {
    object Home : Directory()
    class SubDirectory(val id: String) : Directory()
}