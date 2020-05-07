package com.m3sv.plainupnp.presentation.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

interface ShowDismissListener {
    fun onShow()
    fun onDismiss()
}

enum class ControlsSheetState {
    OPEN, CLOSED
}

@Singleton
class ControlsSheetDelegate @Inject constructor() :
    ShowDismissListener {

    private val _state = MutableLiveData<ControlsSheetState>()

    val state: LiveData<ControlsSheetState> = _state

    override fun onShow() {
        _state.postValue(ControlsSheetState.OPEN)
    }

    override fun onDismiss() {
        _state.postValue(ControlsSheetState.CLOSED)
    }
}
