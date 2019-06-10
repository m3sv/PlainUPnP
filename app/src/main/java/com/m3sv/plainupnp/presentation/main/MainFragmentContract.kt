package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.presentation.main.data.Item

sealed class MainFragmentCommand {
    data class ItemClick(val position: Int) : MainFragmentCommand()
}

sealed class MainFragmentState {
    object Loading : MainFragmentState()
    data class Success(val directoryName: String, val items: List<Item>) : MainFragmentState()
}

class UpnpItem {

}

