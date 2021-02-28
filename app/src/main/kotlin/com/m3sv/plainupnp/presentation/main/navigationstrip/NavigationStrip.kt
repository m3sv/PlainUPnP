package com.m3sv.plainupnp.presentation.main.navigationstrip

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.m3sv.plainupnp.upnp.folder.Folder
import kotlinx.coroutines.flow.MutableSharedFlow

class NavigationStrip : RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val navigationAdapter: NavigationStripAdapter
    private val navigationLayoutManager: LayoutManager

    init {
        isHorizontalScrollBarEnabled = false

        navigationLayoutManager = LinearLayoutManager(context, HORIZONTAL, false)
            .apply { layoutManager = this }

        navigationAdapter = NavigationStripAdapter { clickChannel.tryEmit(it) }
            .apply { adapter = this }
    }

    private val clickChannel = MutableSharedFlow<Folder>()

    val clickFlow = clickChannel

    fun replaceItems(folders: List<Folder>) {
        navigationAdapter.submitList(folders)
        doOnNextLayout {
            if (navigationAdapter.itemCount > 0)
                smoothScrollToPosition(navigationAdapter.itemCount - 1)
        }
    }
}
