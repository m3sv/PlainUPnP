package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.Consumable

sealed class HomeIntention {
    data class ItemClick(val position: Int) : HomeIntention()
    object BackPress : HomeIntention()
}

sealed class Directory {

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
    object Initial : HomeState()
    object Loading : HomeState()
    data class Success(
        val directory: Directory,
        val filterText: Consumable<String>
    ) : HomeState()
}
