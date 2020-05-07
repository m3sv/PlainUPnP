package com.m3sv.plainupnp.presentation.home

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

typealias OnItemClickListener = (Int) -> Unit

open class ItemViewHolder<out T : ViewBinding>(
    val binding: T,
    onItemClickListener: OnItemClickListener
) : RecyclerView.ViewHolder(binding.root) {

    lateinit var item: ContentItem

    init {
        binding.root.setOnClickListener { onItemClickListener(adapterPosition) }
    }

    fun bind(contentItem: ContentItem) {
        this.item = contentItem
    }
}
