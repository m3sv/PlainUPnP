package com.m3sv.plainupnp.upnp.folder

sealed class FolderType(val name: String) {
    class Root(name: String) : FolderType(name)
    class SubFolder(name: String) : FolderType(name)
}
