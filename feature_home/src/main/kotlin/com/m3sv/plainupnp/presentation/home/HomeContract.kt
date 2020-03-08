package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.common.Consumable

sealed class HomeIntention {
    data class ItemClick(val position: Int) : HomeIntention()

    object BackPress : HomeIntention()
}

sealed class Directory {
    object None : Directory()

    data class Root(
        val name: String,
        val content: List<ContentItem>
    ) : Directory()

    data class SubDirectory(
        val parentName: String,
        val content: List<ContentItem>
    ) : Directory()
}

sealed class HomeState {
    object Loading : HomeState()

    data class Success(
        val directory: Directory = Directory.None,
        val enableThumbnails: Boolean = true,
        val filterText: Consumable<String> = Consumable()
    ) : HomeState()
}
