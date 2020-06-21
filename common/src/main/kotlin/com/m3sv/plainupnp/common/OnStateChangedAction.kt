package com.m3sv.plainupnp.common

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

interface OnStateChangedAction {
    fun onStateChanged(sheet: View, newState: Int)
}

class TriggerOnceStateAction(
    private val onStateChanged: (isHidden: Boolean) -> Unit
) : OnStateChangedAction {

    private var hasCalledStateChanged: Boolean = false

    override fun onStateChanged(sheet: View, newState: Int) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            hasCalledStateChanged = false
            onStateChanged(true)
        } else {
            if (!hasCalledStateChanged) {
                hasCalledStateChanged = true
                onStateChanged(false)
            }
        }
    }
}
