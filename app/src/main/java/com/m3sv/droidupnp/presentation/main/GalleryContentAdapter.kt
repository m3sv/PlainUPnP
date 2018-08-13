package com.m3sv.droidupnp.presentation.main

import android.support.annotation.DrawableRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.common.ItemsDiffCallback
import com.m3sv.droidupnp.databinding.GalleryContentItemBinding
import com.m3sv.droidupnp.presentation.base.BaseAdapter
import com.m3sv.droidupnp.presentation.base.BaseViewHolder
import com.m3sv.droidupnp.presentation.main.data.ContentType
import com.m3sv.droidupnp.presentation.main.data.Item


class GalleryContentAdapter :
    BaseAdapter<Item, GalleryContentItemBinding>() {
    override fun createViewHolder(
        layoutInflater: LayoutInflater,
        parent: ViewGroup?
    ): BaseViewHolder<GalleryContentItemBinding> {
        val binding = GalleryContentItemBinding.inflate(layoutInflater, parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<GalleryContentItemBinding>,
        position: Int
    ) {
        val item = items[position]

        holder.binding.run {

            when (item.type) {
                ContentType.IMAGE -> {
                    loadData(holder, item.uri, R.drawable.ic_image)
                }
                ContentType.VIDEO -> {
                    loadData(holder, item.uri, R.drawable.ic_video)
                }
                ContentType.SOUND -> {
                    loadData(holder, item.uri, R.drawable.ic_music)
                }
            }
        }
    }

    private fun loadData(
        holder: BaseViewHolder<GalleryContentItemBinding>,
        data: String, @DrawableRes contentType: Int
    ) {
        Glide.with(holder.itemView.context).load(data).into(holder.binding.thumbnail)
        holder.binding.contentType.setImageResource(contentType)
    }

    class DiffCallback(
        oldItems: List<Item>,
        newItems: List<Item>
    ) :
        ItemsDiffCallback<Item>(oldItems, newItems) {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].uri == newItems[newItemPosition].uri
        }
    }
}