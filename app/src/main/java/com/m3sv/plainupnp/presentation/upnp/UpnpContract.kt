package com.m3sv.plainupnp.presentation.upnp

sealed class MainFragmentCommand {
    data class ItemClick(val position: Int) : MainFragmentCommand()
}

sealed class MainFragmentState {
    object Loading : MainFragmentState()
    data class Success(val directoryName: String, val items: List<Item>) : MainFragmentState()
}