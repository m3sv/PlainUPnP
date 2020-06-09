package com.m3sv.plainupnp.common

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior


class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {

    private val onStateChangedActions: MutableList<OnStateChangedAction> = mutableListOf()

    private val onSlideActions: MutableList<OnSlideAction> = mutableListOf()

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        onSlideActions.forEach { action -> action.onSlide(bottomSheet, slideOffset) }
    }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        onStateChangedActions.forEach { action -> action.onStateChanged(bottomSheet, newState) }
    }

    fun addOnStateChangedAction(action: OnStateChangedAction) {
        onStateChangedActions.add(action)
    }

    fun addOnSlideAction(action: OnSlideAction) {
        onSlideActions.add(action)
    }

}
