package com.m3sv.plainupnp.common

import android.support.v7.util.DiffUtil

abstract class ItemsDiffCallback<T>(
    var oldItems: List<T>,
    var newItems: List<T>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldItems.size

    override fun getNewListSize(): Int = newItems.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldItems[oldItemPosition] == newItems[newItemPosition]
}