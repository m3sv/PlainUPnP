package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.Consumable

sealed class HomeIntention {
    data class ItemClick(val position: Int) : HomeIntention()
}

sealed class HomeState {
    object Loading : HomeState()
    data class Success(
        val directoryName: String,
        val contentItems: List<ContentItem>
    ) : HomeState()

    data class Exit(val root: Success, val showExitDialog: Consumable<Unit>) : HomeState()
}
