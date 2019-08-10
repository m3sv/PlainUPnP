package com.m3sv.plainupnp.presentation.base

import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.m3sv.plainupnp.common.ItemsDiffCallback
import kotlin.properties.Delegates

abstract class BaseAdapter<T>(private val diffCallback: ItemsDiffCallback<T>) :
        RecyclerView.Adapter<ItemViewHolder<*>>() {
    private var originalItems = listOf<T>()

    var items: List<T> by Delegates.observable(mutableListOf()) { _, _, newValue ->
        if (newValue.isEmpty())
            isEmpty.postValue(true)
        else
            isEmpty.postValue(false)
    }

    private val isEmpty: MutableLiveData<Boolean> = MutableLiveData()

    override fun getItemCount(): Int = items.size

    fun setWithDiff(newItems: List<T>) {
        originalItems = newItems

        diffCallback.oldItems = items
        diffCallback.newItems = newItems

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        items = diffCallback.newItems
    }


    fun resetItems() {
        setWithDiff(originalItems)
    }

    fun filterWithDiff(predicate: (T) -> Boolean) {
        diffCallback.oldItems = diffCallback.newItems
        diffCallback.newItems = originalItems.filter(predicate)

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        items = diffCallback.newItems
    }

    /**
     * Removes item at the specified position and returns modified contentItems list
     */
    fun removeAt(position: Int) = items.toMutableList().apply { removeAt(position) }
}
