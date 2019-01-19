package com.m3sv.plainupnp.data.upnp


sealed class Directory(val name: String) {
    class Home(name: String) : Directory(name)
    class SubDirectory(val id: String, name: String, val parentId: String?) : Directory(name)
}