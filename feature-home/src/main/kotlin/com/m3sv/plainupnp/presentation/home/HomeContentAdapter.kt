package com.m3sv.plainupnp.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.RequestManager
import com.m3sv.plainupnp.common.ItemsDiffCallback
import com.m3sv.plainupnp.presentation.home.databinding.FolderItemBinding
import com.m3sv.plainupnp.presentation.home.databinding.MediaItemBinding
import java.util.*

class GalleryContentAdapter(
    private val glide: RequestManager,
    private val showThumbnails: ShowThumbnailsUseCase,
    private val onItemClickListener: OnItemClickListener
) : BaseAdapter<ContentItem>(diffCallback) {

    override fun getItemViewType(position: Int): Int = items[position].type.ordinal

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder<ViewBinding> = when (ContentType.values()[viewType]) {
        ContentType.FOLDER -> ItemViewHolder(
            binding = FolderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ) as ViewBinding,
            onItemClickListener = onItemClickListener
        )

        else -> ItemViewHolder(
            binding = MediaItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            onItemClickListener = onItemClickListener
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder<ViewBinding>, position: Int) {
        val item = items[position]
        holder.bind(item)

        when (item.type) {
            ContentType.FOLDER -> loadFolder(holder)
            else -> loadData(holder)
        }
    }

    suspend fun filter(text: CharSequence) {
        if (text.isEmpty()) {
            resetItems()
            return
        }

        filterWithDiff { it.name.toLowerCase(Locale.getDefault()).contains(text) }
    }

    private fun loadData(holder: ItemViewHolder<ViewBinding>) {
        with(holder.extractBinding<MediaItemBinding>()) {
            when (holder.item.type) {
                ContentType.IMAGE,
                ContentType.VIDEO -> {
                    if (showThumbnails()) {
                        glide.load(holder.item.itemUri).into(thumbnail)
                    } else {
                        thumbnail.setImageResource(holder.item.icon)
                    }
                }
                else -> thumbnail.setImageResource(holder.item.icon)
            }
            title.text = holder.item.name
        }
    }

    private fun loadFolder(holder: ItemViewHolder<*>) {
        with(holder.extractBinding<FolderItemBinding>()) {
            thumbnail.setImageResource(R.drawable.ic_folder)
            title.text = holder.item.name
        }
    }

    private fun <T : ViewBinding> ItemViewHolder<*>.extractBinding(): T =
        (this as ItemViewHolder<T>).binding

    companion object {
        private val diffCallback = DiffCallback(listOf(), listOf())
    }
}

private class DiffCallback(
    oldContentItems: List<ContentItem>,
    newContentItems: List<ContentItem>
) : ItemsDiffCallback<ContentItem>(oldContentItems, newContentItems) {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldItems[oldItemPosition].itemUri == newItems[newItemPosition].itemUri
}
