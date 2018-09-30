package com.m3sv.droidupnp.presentation.main

import android.support.annotation.DrawableRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.m3sv.droidupnp.R
import com.m3sv.droidupnp.common.ItemsDiffCallback
import com.m3sv.droidupnp.databinding.GalleryContentFolderItemBinding
import com.m3sv.droidupnp.databinding.GalleryContentItemBinding
import com.m3sv.droidupnp.presentation.base.BaseAdapter
import com.m3sv.droidupnp.presentation.base.BaseViewHolder
import com.m3sv.droidupnp.presentation.main.data.ContentType
import com.m3sv.droidupnp.presentation.main.data.Item
import org.droidupnp.model.upnp.didl.IDIDLItem


interface OnItemClickListener {
    fun onDirectoryClick(itemUri: String, parentId: String?)

    fun onItemClick(item: IDIDLItem)
}

class GalleryContentAdapter(private val onItemClickListener: OnItemClickListener) :
    BaseAdapter<Item>(GalleryContentAdapter.diffCallback) {

    override fun getItemViewType(position: Int): Int = items[position].type.ordinal

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder<*> = when (ContentType.values()[viewType]) {
        ContentType.DIRECTORY -> BaseViewHolder(
            GalleryContentFolderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        else -> BaseViewHolder(
            GalleryContentItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = items[position]

        val itemClickListener = View.OnClickListener {
            item.didlObjectDisplay?.get(holder.adapterPosition)?.let {
                onItemClickListener.onItemClick(it.didlObject as IDIDLItem)
            }
        }

        when (item.type) {
            ContentType.IMAGE -> {
                loadData(holder, item, R.drawable.ic_image, itemClickListener)
            }
            ContentType.VIDEO -> {
                loadData(holder, item, R.drawable.ic_video, itemClickListener)
            }
            ContentType.AUDIO -> {
                loadData(holder, item, R.drawable.ic_music, itemClickListener)
            }
            ContentType.DIRECTORY -> {
                loadDirectory(holder, item)
            }
        }
    }

    private fun loadData(
        holder: BaseViewHolder<*>,
        item: Item,
        @DrawableRes contentTypeIcon: Int,
        onClick: View.OnClickListener
    ) {
        with((holder as BaseViewHolder<GalleryContentItemBinding>).binding) {
            Glide.with(holder.itemView.context).load(item.uri).into(thumbnail)
            title.text = item.name
            title.setOnClickListener(onClick)
            thumbnail.setOnClickListener(onClick)

            contentType.setImageResource(contentTypeIcon)
            contentType.setOnClickListener(onClick)
        }
    }

    private fun loadDirectory(
        holder: BaseViewHolder<*>,
        item: Item
    ) {
        with((holder as BaseViewHolder<GalleryContentFolderItemBinding>).binding) {
            title.text = item.name
            thumbnail.setImageResource(R.drawable.ic_folder)
            container.setOnClickListener {
                onItemClickListener.onDirectoryClick(
                    item.uri,
                    item.parentId
                )
            }
        }
    }

    fun filter(text: CharSequence) {
        if (text.isEmpty()) {
            resetItems()
            return
        }

        filterWithDiff { it.name.toLowerCase().contains(text) }
    }

    companion object {
        val diffCallback = DiffCallback(listOf(), listOf())
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