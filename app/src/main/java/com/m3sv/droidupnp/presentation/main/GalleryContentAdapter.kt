package com.m3sv.droidupnp.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.common.ItemsDiffCallback
import com.m3sv.droidupnp.databinding.GalleryContentItemBinding
import com.m3sv.droidupnp.presentation.base.BaseAdapter
import com.m3sv.droidupnp.presentation.base.BaseViewHolder
import com.m3sv.droidupnp.presentation.main.data.ContentType
import com.m3sv.droidupnp.presentation.main.data.Item


class GalleryContentAdapter :
    BaseAdapter<GalleryRepository.ImageInfo, GalleryContentItemBinding>() {
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
            Glide.with(holder.itemView.context).load(item.data).into(thumbnail)
            contentType.setImageResource(R.drawable.ic_image)
        }
    }

    class DiffCallback(
        oldItems: List<GalleryRepository.ImageInfo>,
        newItems: List<GalleryRepository.ImageInfo>
    ) :
        ItemsDiffCallback<GalleryRepository.ImageInfo>(oldItems, newItems) {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].data == newItems[newItemPosition].data
        }
    }
}