package com.m3sv.plainupnp.presentation.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.m3sv.plainupnp.presentation.upnp.OnItemClickListener
import com.m3sv.plainupnp.presentation.upnp.Item

open class ItemViewHolder<out T : ViewDataBinding>(val binding: T, onItemClickListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

    private lateinit var item: Item

    init {
        binding.root.setOnClickListener {
            onItemClickListener(adapterPosition)
        }
    }

    fun bind(item: Item) {
        this.item = item
    }
}