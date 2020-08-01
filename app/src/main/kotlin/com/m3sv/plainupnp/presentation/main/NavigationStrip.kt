package com.m3sv.plainupnp.presentation.main

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.size
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.upnp.folder.FolderType
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.asFlow

class NavigationStrip : HorizontalScrollView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    private val contentLayout: LinearLayout

    init {
        isHorizontalScrollBarEnabled = false

        contentLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        addView(contentLayout)
    }

    private val clickChannel = BroadcastChannel<FolderType>(1)

    val clickFlow = clickChannel.asFlow()

    fun addItem(folder: FolderType) {
        val item = TextView(context).apply {
            text = folder.title
            tag = folder.folderId

            layoutParams = MarginLayoutParams(
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            ).apply {
                leftMargin = 16
                rightMargin = 16
            }

            setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_next_folder,
                0,
                0,
                0
            )
            gravity = Gravity.CENTER

            setOnClickListener {
                clickChannel.offer(folder)
            }
        }

        contentLayout.addView(item)
    }

    fun clearItems() {
        contentLayout.removeAllViews()
    }

    fun replaceItems(names: List<FolderType>) {
        clearItems()
        addItems(names)

        post {
            smoothScrollTo(width, 0)
        }
    }

    fun addItems(names: List<FolderType>) {
        for (name in names) {
            addItem(name)
        }
    }

    fun isEmpty() = contentLayout.childCount == 0

    fun popLast() {
        if (contentLayout.size == 0) {
            error("Content is empty!")
        }

        contentLayout.removeViewAt(contentLayout.size - 1)
    }
}
