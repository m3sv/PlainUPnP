package com.m3sv.droidupnp.presentation.main

import android.support.annotation.DrawableRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.common.ItemsDiffCallback
import com.m3sv.droidupnp.databinding.GalleryContentItemBinding
import com.m3sv.droidupnp.presentation.base.BaseAdapter
import com.m3sv.droidupnp.presentation.base.BaseViewHolder
import com.m3sv.droidupnp.presentation.main.data.ContentType
import com.m3sv.droidupnp.presentation.main.data.Item
import com.m3sv.droidupnp.upnp.DIDLObjectDisplay
import timber.log.Timber


class GalleryContentAdapter(private val onClick: (String) -> Unit) :
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
                    loadData(holder, item.uri, item.name, R.drawable.ic_image)
                }
                ContentType.VIDEO -> {
                    loadData(holder, item.uri, item.name, R.drawable.ic_video)
                }
                ContentType.SOUND -> {
                    loadData(holder, item.uri, item.name, R.drawable.ic_music)
                }
                ContentType.DIRECTORY -> {
                    loadDirectory(holder, item.uri, item.name, item.didlObjectDisplay)
                }
            }

            val itemClickListener = View.OnClickListener {
                Timber.d("On item clicked: uri:${item.uri}, name:${item.name}")
            }

            thumbnail.setOnClickListener(itemClickListener)
            contentType.setOnClickListener(itemClickListener)
        }
    }

    private fun loadData(
        holder: BaseViewHolder<GalleryContentItemBinding>,
        data: String,
        title: String,
        @DrawableRes contentType: Int
    ) {
        Glide.with(holder.itemView.context).load(data).into(holder.binding.thumbnail)
        holder.binding.title.text = title
        holder.binding.contentType.setImageResource(contentType)
    }

    private fun loadDirectory(
        holder: BaseViewHolder<GalleryContentItemBinding>,
        uri: String,
        title: String,
        item: List<DIDLObjectDisplay>?
    ) {
//        Glide.with(holder.itemView.context).load(R.drawable.ic_folder).into(holder.binding.thumbnail)
        holder.binding.title.text = title
        holder.binding.thumbnail.setImageResource(R.drawable.ic_folder)
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