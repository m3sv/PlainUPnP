package com.m3sv.plainupnp.presentation.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.utils.dp
import com.m3sv.plainupnp.common.utils.isRunningOnTv


class FabMenu : LinearLayout {
    constructor(context: Context) : super(context, null) {
        if (context.isRunningOnTv())
            initializeTvLayout(context)
        else
            initializeMobileLayout(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
        if (context.isRunningOnTv())
            initializeTvLayout(context)
        else
            initializeMobileLayout(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        if (context.isRunningOnTv())
            initializeTvLayout(context)
        else
            initializeMobileLayout(context)
    }

    private lateinit var searchButton: FloatingActionButton

    private lateinit var expandMenuButton: FloatingActionButton

    private lateinit var settingsButton: FloatingActionButton

    private fun initializeTvLayout(context: Context) {
        orientation = VERTICAL

        val tv = TypedValue()
        val height = if (context.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        } else LayoutParams.WRAP_CONTENT

        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, height)

        expandMenuButton = createFab(false, true, R.drawable.ic_add) { toggleMenuState() }
        searchButton = createFab(true, true, R.drawable.ic_search)
        settingsButton = createFab(true, true, R.drawable.ic_settings_white)

        expandMenuButton.nextFocusDownId = searchButton.id
        searchButton.nextFocusDownId = settingsButton.id

        addView(expandMenuButton)
        addView(searchButton)
        addView(settingsButton)
    }

    private lateinit var mobileSearchButton: ImageView

    private fun initializeMobileLayout(context: Context) {
        mobileSearchButton = ImageView(context).apply {
            setImageResource(R.drawable.ic_search_half_grey)
            val topMargin = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 12.dp else 8.dp
            layoutParams = MarginLayoutParams(32.dp, 32.dp).also { it.setMargins(8.dp, topMargin, 8.dp, 8.dp) }
        }

        addView(mobileSearchButton)
    }

    @SuppressLint("RestrictedApi")
    private fun createFab(isHidden: Boolean,
                          clickable: Boolean,
                          @DrawableRes icon: Int,
                          @ColorRes backgroundTint: Int = R.color.colorPrimary,
                          clickListener: ((View) -> Unit)? = null): FloatingActionButton {
        return FloatingActionButton(context).apply {
            if (isHidden) visibility = View.INVISIBLE

            isClickable = clickable

            backgroundTintList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ColorStateList.valueOf(resources.getColor(backgroundTint, context.theme))
            } else {
                ColorStateList.valueOf(resources.getColor(backgroundTint))
            }

            setOnClickListener(clickListener)
            setImageResource(icon)

            layoutParams = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT).also { it.setMargins(8.dp, 8.dp, 8.dp, 8.dp) }
        }
    }

    fun setOnSearchClickListener(clickListener: (View) -> Unit) {
        if (context.isRunningOnTv()) {
            searchButton.setOnClickListener(clickListener)
        } else {
            mobileSearchButton.setOnClickListener(clickListener)
        }
    }

    fun setOnSettingsClickListener(clickListener: (View) -> Unit) = settingsButton.setOnClickListener(clickListener)

    private var shown = false

    private fun toggleMenuState() {
        shown = if (shown) {
            searchButton.hide()
            settingsButton.hide()

            false
        } else {
            searchButton.show()

            postDelayed({
                if (shown) settingsButton.show()
            }, 150)

            true
        }
    }
}