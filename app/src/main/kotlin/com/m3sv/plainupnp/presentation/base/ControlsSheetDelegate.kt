package com.m3sv.plainupnp.presentation.base

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import javax.inject.Inject

interface ShowDismissListener {
    fun onShow()
    fun onDismiss()
}

enum class ControlsSheetState {
    OPEN, CLOSED
}

class ControlsSheetDelegate @Inject constructor() : ViewModel(), ShowDismissListener {

    private val _state = MutableLiveData<ControlsSheetState>()

    val state: LiveData<ControlsSheetState> = _state

    override fun onShow() {
        _state.postValue(ControlsSheetState.OPEN)
    }

    override fun onDismiss() {
        _state.postValue(ControlsSheetState.CLOSED)
    }

    companion object {
        fun get(activity: FragmentActivity): ControlsSheetDelegate =
            ViewModelProviders.of(activity)[ControlsSheetDelegate::class.java]
    }
}
