package com.m3sv.plainupnp.presentation.main

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.m3sv.plainupnp.common.R
import com.m3sv.plainupnp.common.databinding.VolumeIndicatorBinding

class VolumeIndicator : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private var binding: VolumeIndicatorBinding =
        VolumeIndicatorBinding.inflate(LayoutInflater.from(context), this)

    private val rootView =
        (context as Activity).window.decorView.findViewById<ViewGroup>(android.R.id.content)

    private val layoutParams =
        MarginLayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            val margin = resources.getDimension(R.dimen.volume_indicator_margin).toInt()
            marginStart = margin
            marginEnd = margin
            topMargin = margin
            bottomMargin = margin
        }

    private val slideAnimation = Slide(Gravity.TOP)

    private val volumeHandler = Handler()

    private val hideAnimationRunnable = Runnable {
        TransitionManager.beginDelayedTransition(rootView, slideAnimation)
        rootView.removeView(this)
    }

    init {
        background = ContextCompat.getDrawable(
            context,
            R.drawable.bg_rounded_corners
        )
    }

    var volume: Int = 0
        set(value) {
            binding.volumeBar.progress = value
            clearHandler()
            postHideViewMessage()
            showView()
            field = value
        }

    private fun showView() {
        if (rootView.indexOfChild(this) == -1) {
            this.alpha = 1f
            TransitionManager.beginDelayedTransition(rootView, slideAnimation)
            rootView.addView(this, layoutParams)
        }
    }

    private fun postHideViewMessage() {
        volumeHandler.postDelayed(hideAnimationRunnable, 2000L)
    }

    private fun clearHandler() {
        volumeHandler.removeCallbacks(hideAnimationRunnable)
    }
}
