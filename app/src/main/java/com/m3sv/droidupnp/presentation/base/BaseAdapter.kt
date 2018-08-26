package com.m3sv.droidupnp.presentation.base

import android.arch.lifecycle.MutableLiveData
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import com.m3sv.droidupnp.common.ItemsDiffCallback
import kotlin.properties.Delegates

abstract class BaseAdapter<T>(private val diffCallback: ItemsDiffCallback<T>) :
    RecyclerView.Adapter<BaseViewHolder<*>>() {
    var originalItems = listOf<T>()

    var items: List<T> by Delegates.observable(mutableListOf()) { _, _, newValue ->
        if (newValue.isEmpty())
            isEmpty.postValue(true)
        else
            isEmpty.postValue(false)
    }

    val isEmpty: MutableLiveData<Boolean> = MutableLiveData()

    override fun getItemCount(): Int = items.size

    fun setWithDiff(newItems: List<T>) {
        originalItems = newItems

        diffCallback.oldItems = items
        diffCallback.newItems = newItems

        if (diffCallback.newItems.isEmpty()) {
            items = listOf()
            diffCallback.oldItems = items
            notifyDataSetChanged()
            return
        }

        if (items.isEmpty()) {
            items = diffCallback.newItems
            notifyDataSetChanged()
            return
        }

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

        if (diffCallback.newItems.isEmpty()) {
            items = listOf()
            notifyDataSetChanged()
            return
        }

        if (items.isEmpty()) {
            items = diffCallback.newItems
            notifyDataSetChanged()
            return
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        diffResult.dispatchUpdatesTo(this)
        items = diffCallback.newItems
    }

    /**
     * Removes item at the specified position and returns modified items list
     */
    fun removeAt(position: Int) = items.toMutableList().apply { removeAt(position) }
}
