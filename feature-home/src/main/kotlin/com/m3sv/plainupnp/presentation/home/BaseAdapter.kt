package com.m3sv.plainupnp.presentation.home

import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.m3sv.plainupnp.common.ItemsDiffCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private var filterJob: Job? = null

    suspend fun filterWithDiff(predicate: (T) -> Boolean) {
        filterJob?.cancel()
        filterJob = withContext(Dispatchers.IO) {
            launch {
                diffCallback.oldItems = diffCallback.newItems
                diffCallback.newItems = originalItems.filter(predicate)

                val diffResult = DiffUtil.calculateDiff(diffCallback)

                withContext(Dispatchers.Main) {
                    diffResult.dispatchUpdatesTo(this@BaseAdapter)
                    items = diffCallback.newItems
                }
            }
        }
    }

    /**
     * Removes item at the specified position and returns modified contentItems list
     */
    fun removeAt(position: Int) = items.toMutableList().apply { removeAt(position) }
}
