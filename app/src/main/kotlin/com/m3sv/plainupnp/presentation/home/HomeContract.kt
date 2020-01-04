package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.Consumable

sealed class HomeIntention {
    data class ItemClick(val position: Int) : HomeIntention()
    object BackPress : HomeIntention()
}

sealed class HomeState {
    object Initial : HomeState()
    object Loading : HomeState()
    data class Success(
        val directoryName: String,
        val contentItems: List<ContentItem>,
        val isRoot: Boolean,
        val filterText: Consumable<String>
    ) : HomeState()
}
