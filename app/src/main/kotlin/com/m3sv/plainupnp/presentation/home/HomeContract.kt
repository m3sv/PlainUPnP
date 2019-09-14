package com.m3sv.plainupnp.presentation.home

sealed class MainFragmentIntention {
    data class ItemClick(val position: Int) : MainFragmentIntention()
}

sealed class MainFragmentState {
    object Loading : MainFragmentState()
    data class Success(
        val directoryName: String,
        val contentItems: List<ContentItem>
    ) : MainFragmentState()
}