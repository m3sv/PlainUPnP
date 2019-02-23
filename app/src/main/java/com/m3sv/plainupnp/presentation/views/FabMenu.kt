package com.m3sv.plainupnp.presentation.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.m3sv.plainupnp.R

class FabMenu : LinearLayout {

    constructor(context: Context) : super(context, null) {
        initialize(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, 0) {
        initialize(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize(context, attrs)
    }

    lateinit var fab1: FloatingActionButton
    lateinit var fab2: FloatingActionButton
    lateinit var fab3: FloatingActionButton

    private fun initialize(context: Context, attrs: AttributeSet?) {
        fab1 = FloatingActionButton(context, attrs).apply {
            isClickable = true
            setOnClickListener {
                toggle()
            }
        }
        fab2 = FloatingActionButton(context, attrs).apply {
            visibility = View.INVISIBLE
            backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorPrimaryLight))
            isClickable = true
        }
        fab3 = FloatingActionButton(context, attrs).apply { hide() }

        orientation = LinearLayout.VERTICAL
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)


        addView(fab1)
        addView(fab2)
        addView(fab3)
    }

    var shown = false


    fun toggle() {
        shown = if (shown) {
            fab2.hide()
            fab3.hide()

            false
        } else {
            fab2.show()

            postDelayed({
                if (shown)
                    fab3.show()
            }, 150)

            true
        }


    }
}