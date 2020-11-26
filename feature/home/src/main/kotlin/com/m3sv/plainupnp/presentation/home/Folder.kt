package com.m3sv.plainupnp.presentation.home


sealed class UpnpFolder {
    object None : UpnpFolder()

    data class Root(
        val name: String,
        val content: List<ContentItem>
    ) : UpnpFolder()

    data class SubFolder(
        val parentName: String,
        val content: List<ContentItem>
    ) : UpnpFolder()
}
