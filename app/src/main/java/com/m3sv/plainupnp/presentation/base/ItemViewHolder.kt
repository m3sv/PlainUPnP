package com.m3sv.plainupnp.presentation.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.m3sv.plainupnp.data.upnp.DIDLItem
import com.m3sv.plainupnp.presentation.main.OnItemClickListener
import com.m3sv.plainupnp.presentation.main.data.Item

open class ItemViewHolder<out T : ViewDataBinding>(val binding: T, onItemClickListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

    private var item: Item? = null

    init {
        binding.root.setOnClickListener {
            adapterPosition.takeIf { it >= 0 }?.let { adapterPosition ->
                item?.didlObjectDisplay?.get(adapterPosition)?.let {
                    onItemClickListener.onItemClick(it.didlObject as DIDLItem, adapterPosition)
                }
            }
        }
    }

    fun bind(item: Item) {
        this.item = item
    }
}