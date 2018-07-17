package com.m3sv.droidupnp.presentation.base

import android.arch.lifecycle.MutableLiveData
import android.databinding.ViewDataBinding
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.m3sv.droidupnp.common.ItemsDiffCallback
import kotlin.properties.Delegates

abstract class BaseAdapter<T, B : ViewDataBinding> : RecyclerView.Adapter<BaseViewHolder<B>>() {
    var items: List<T> by Delegates.observable(mutableListOf()) { _, _, newValue ->
        if (newValue.isEmpty())
            isEmpty.postValue(true)
        else
            isEmpty.postValue(false)
    }

    val isEmpty: MutableLiveData<Boolean> = MutableLiveData()

    abstract fun createViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): BaseViewHolder<B>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<B> {
        return createViewHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun getItemCount(): Int = items.size

    fun setWithDiff(diffCallback: ItemsDiffCallback<T>) {
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
