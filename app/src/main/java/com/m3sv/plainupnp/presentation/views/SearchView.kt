package com.m3sv.plainupnp.presentation.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.utils.dp


class SearchView : LinearLayout {
    constructor(context: Context) : super(context, null) {
        initializeMobileLayout(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
        initializeMobileLayout(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializeMobileLayout(context)
    }

    private lateinit var searchButton: ImageView

    private fun initializeMobileLayout(context: Context) {

        gravity = Gravity.CENTER

        searchButton = ImageView(context).apply {
            setImageResource(R.drawable.ic_search_half_grey)
            layoutParams = ViewGroup.LayoutParams(32.dp, 32.dp)
        }

        addView(searchButton)
    }

    fun setOnSearchClickListener(clickListener: (View) -> Unit) {
        searchButton.setOnClickListener(clickListener)
    }
}