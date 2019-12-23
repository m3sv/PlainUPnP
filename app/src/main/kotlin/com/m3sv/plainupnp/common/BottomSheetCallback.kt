package com.m3sv.plainupnp.common

import android.annotation.SuppressLint
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.m3sv.plainupnp.common.utils.normalize
import kotlin.math.max


class BottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {

    private val onStateChangedActions: MutableList<OnStateChangedAction> = mutableListOf()

    private val onSlideActions: MutableList<OnSlideAction> = mutableListOf()

    private var halfExpandedSlideOffset = Float.MAX_VALUE

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        if (halfExpandedSlideOffset == Float.MAX_VALUE)
            calculateInitialHalfExpandedSlideOffset(bottomSheet)

        // Correct for the fact that the slideOffset is not zero when half expanded
        val trueOffset = if (slideOffset <= halfExpandedSlideOffset) {
            slideOffset.normalize(-1F, halfExpandedSlideOffset, -1F, 0F)
        } else {
            slideOffset.normalize(halfExpandedSlideOffset, 1F, 0F, 1F)
        }

        onSlideActions.forEach { it.onSlide(bottomSheet, trueOffset) }
    }

    /**
     * Calculate the onSlideOffset which will be given when the bottom sheet is in the
     * [BottomSheetBehavior.STATE_HALF_EXPANDED] state.
     *
     * Recording the correct slide offset for the half expanded state happens in [onStateChanged].
     * Since the first time the sheet is opened, we haven't yet received a call to [onStateChanged],
     * this method is used to calculate the initial value manually so we can smoothly normalize
     * slideOffset values received between -1 and 1.
     *
     * See:
     * [BottomSheetBehavior.calculateCollapsedOffset]
     * [BottomSheetBehavior.calculateHalfExpandedOffset]
     * [BottomSheetBehavior.dispatchOnSlide]
     */
    @SuppressLint("PrivateResource")
    private fun calculateInitialHalfExpandedSlideOffset(sheet: View) {
        val parent = sheet.parent as CoordinatorLayout
        val behavior = BottomSheetBehavior.from(sheet)

        val halfExpandedOffset = parent.height * (1 - behavior.halfExpandedRatio)
        val peekHeightMin = parent.resources.getDimensionPixelSize(
            R.dimen.design_bottom_sheet_peek_height_min
        )
        val peek = max(peekHeightMin, parent.height - parent.width * 9 / 16)
        val collapsedOffset = max(
            parent.height - peek,
            max(0, parent.height - sheet.height)
        )
        halfExpandedSlideOffset =
            (collapsedOffset - halfExpandedOffset) / (parent.height - collapsedOffset)
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
