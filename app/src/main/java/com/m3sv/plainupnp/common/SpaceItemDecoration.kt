package com.m3sv.plainupnp.common

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import android.view.View


class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
    ) {
        with(outRect) {
            left = space
            right = space
            bottom = space
        }
    }
}

