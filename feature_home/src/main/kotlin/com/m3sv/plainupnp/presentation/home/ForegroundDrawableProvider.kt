package com.m3sv.plainupnp.presentation.home

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.TypedValue

class ForegroundDrawableProvider(private val context: Context) {
    val drawable: Drawable
        get() {
            val selectableItemBackground =
                TypedValue().apply {
                    context.theme.resolveAttribute(
                        android.R.attr.selectableItemBackground,
                        this,
                        true
                    )
                }.resourceId

            return requireNotNull(context.getDrawable(selectableItemBackground))
        }

}
