package com.m3sv.plainupnp.presentation.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import com.m3sv.plainupnp.common.utils.dp
import kotlin.math.roundToInt

class OffsetItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private var divider: Drawable? = null

    private val bounds = Rect()

    init {
        val a = context.obtainStyledAttributes(ATTRS)
        divider = a.getDrawable(0)
        a.recycle()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (divider == null)
            return

        drawVertical(c, parent)
    }

    private fun drawVertical(canvas: Canvas, parent: RecyclerView) {
        canvas.save()

        val left: Int
        val right: Int

        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(
                left, parent.paddingTop, right,
                parent.height - parent.paddingBottom
            )
        } else {
            left = 116.dp
            right = parent.width
        }

        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, bounds)
            val bottom = bounds.bottom + child.translationY.roundToInt()
            val top = bottom - (divider?.intrinsicHeight ?: 0)

            divider?.let {
                it.setBounds(left, top, right, bottom)
                it.draw(canvas)
            }
        }
        canvas.restore()
    }


    companion object {
        private val ATTRS = intArrayOf(android.R.attr.listDivider)
    }
}
