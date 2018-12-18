package com.m3sv.plainupnp.presentation.main

import android.support.annotation.DrawableRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.ItemsDiffCallback
import com.m3sv.plainupnp.data.upnp.DIDLItem
import com.m3sv.plainupnp.databinding.GalleryContentFolderItemBinding
import com.m3sv.plainupnp.databinding.GalleryContentItemBinding
import com.m3sv.plainupnp.presentation.base.BaseAdapter
import com.m3sv.plainupnp.presentation.base.BaseViewHolder
import com.m3sv.plainupnp.presentation.main.data.ContentType
import com.m3sv.plainupnp.presentation.main.data.Item


interface OnItemClickListener {
    fun onDirectoryClick(itemUri: String?, parentId: String?)

    fun onItemClick(item: DIDLItem, position: Int)
}

class GalleryContentAdapter(private val onItemClickListener: OnItemClickListener) :
    BaseAdapter<Item>(GalleryContentAdapter.diffCallback) {

    var clickable = true

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
            if (clickable)
                holder.adapterPosition.takeIf { it >= 0 }?.let { adapterPosition ->
                    item.didlObjectDisplay?.get(adapterPosition)?.let {
                        onItemClickListener.onItemClick(
                            it.didlObject as DIDLItem,
                            holder.adapterPosition
                        )
                    }
                }
        }

        when (item.type) {
            ContentType.IMAGE -> loadData(
                holder,
                item,
                R.drawable.ic_image,
                itemClickListener,
                RequestOptions()
            )
            ContentType.VIDEO -> loadData(
                holder,
                item,
                R.drawable.ic_video,
                itemClickListener,
                RequestOptions()
            )
            ContentType.AUDIO -> loadData(
                holder,
                item,
                R.drawable.ic_music,
                itemClickListener,
                RequestOptions().placeholder(R.drawable.ic_music_note)
            )

            ContentType.DIRECTORY -> loadDirectory(holder, item)
        }
    }

    private fun loadData(
        holder: BaseViewHolder<*>,
        item: Item,
        @DrawableRes contentTypeIcon: Int,
        onClick: View.OnClickListener,
        requestOptions: RequestOptions
    ) {
        with((holder as BaseViewHolder<GalleryContentItemBinding>).binding) {
            Glide.with(holder.itemView)
                .load(item.uri)
                .thumbnail(0.25f)
                .apply(requestOptions)
                .into(thumbnail)

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
                if (clickable)
                    onItemClickListener.onDirectoryClick(item.uri, item.parentId)
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


    class DiffCallback(
        oldItems: List<Item>,
        newItems: List<Item>
    ) :
        ItemsDiffCallback<Item>(oldItems, newItems) {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].uri == newItems[newItemPosition].uri
        }
    }

    companion object {
        val diffCallback = DiffCallback(listOf(), listOf())
    }
}