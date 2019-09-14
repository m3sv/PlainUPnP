package com.m3sv.plainupnp.presentation.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.m3sv.plainupnp.presentation.home.ContentItem
import com.m3sv.plainupnp.presentation.home.OnItemClickListener

open class ItemViewHolder<out T : ViewDataBinding>(val binding: T, onItemClickListener: OnItemClickListener) :
        RecyclerView.ViewHolder(binding.root) {

    private lateinit var contentItem: ContentItem

    init {
        binding.root.setOnClickListener {
            onItemClickListener(adapterPosition)
        }
    }

    fun bind(contentItem: ContentItem) {
        this.contentItem = contentItem
    }
}