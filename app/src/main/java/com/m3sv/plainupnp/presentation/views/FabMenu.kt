package com.m3sv.plainupnp.presentation.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
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
        orientation = LinearLayout.VERTICAL
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        expandMenuButton = createFab(context, false, true, R.drawable.ic_add) { toggleMenuState() }
        searchButton = createFab(context, true, true, R.drawable.ic_search)
        settingsButton = createFab(context, true, true, R.drawable.ic_settings_white)

        expandMenuButton.nextFocusDownId = searchButton.id
        searchButton.nextFocusDownId = settingsButton.id

        addView(expandMenuButton)
        addView(searchButton)
        addView(settingsButton)
    }

    private fun createFab(context: Context,
                          isHidden: Boolean,
                          clickable: Boolean,
                          @DrawableRes icon: Int,
                          @ColorRes backgroundTint: Int = R.color.colorPrimary,
                          clickListener: ((View) -> Unit)? = null): FloatingActionButton {
        return FloatingActionButton(context).apply {
            if (isHidden)
                visibility = View.INVISIBLE

            isClickable = clickable
            backgroundTintList = ColorStateList.valueOf(resources.getColor(backgroundTint))
            setOnClickListener(clickListener)
            setImageDrawable(resources.getDrawable(icon))

            layoutParams = MarginLayoutParams(WRAP_CONTENT, WRAP_CONTENT).also { it.setMargins(8.dp, 8.dp, 8.dp, 8.dp) }
        }
    }

    fun setOnSearchClickListener(clickListener: (View) -> Unit) = searchButton.setOnClickListener(clickListener)

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
                if (shown)
                    settingsButton.show()
            }, 150)

            true
        }
    }

    private fun initializeMobileLayout(context: Context) {
        searchButton = createFab(context, false, true, R.drawable.ic_search)
    }
}