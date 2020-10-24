package com.m3sv.plainupnp.common

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import com.m3sv.plainupnp.common.databinding.PopupBubbleBinding

fun Context.showPopupBubbleAt(
    anchor: View,
    offsetX: Int = 0,
    offsetY: Int = 0,
    text: String,
): PopupWindow {
    val binding = PopupBubbleBinding
        .inflate(LayoutInflater.from(this))
        .apply {
            infoText.text = text
            root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        }

    return PopupWindow(
        binding.root,
        binding.root.measuredWidth,
        binding.root.measuredHeight
    ).apply {
        showAsDropDown(anchor, offsetX, offsetY)
        binding.root.setOnClickListener { dismiss() }
    }
}
