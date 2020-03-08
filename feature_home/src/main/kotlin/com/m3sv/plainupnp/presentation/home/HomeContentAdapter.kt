package com.m3sv.plainupnp.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.ListPreloader
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.util.FixedPreloadSizeProvider
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.ItemsDiffCallback
import com.m3sv.plainupnp.common.utils.dp
import com.m3sv.plainupnp.databinding.DirectoryItemBinding
import com.m3sv.plainupnp.databinding.MediaItemBinding
import java.util.*


class GalleryContentAdapter(
    private val glide: RequestManager,
    private val onItemClickListener: OnItemClickListener
) : BaseAdapter<ContentItem>(diffCallback) {

    var showThumbnails: Boolean = true

    override fun getItemViewType(position: Int): Int = items[position].type.ordinal

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder<ViewBinding> = when (ContentType.values()[viewType]) {
        ContentType.DIRECTORY -> ItemViewHolder(
            binding = DirectoryItemBinding.inflate(
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
            ContentType.DIRECTORY -> loadDirectory(holder, item)
            else -> loadData(holder, item)
        }
    }

    fun filter(text: CharSequence) {
        if (text.isEmpty()) {
            resetItems()
            return
        }

        filterWithDiff { it.name.toLowerCase(Locale.getDefault()).contains(text) }
    }

    private fun loadData(
        holder: ItemViewHolder<ViewBinding>,
        contentItem: ContentItem
    ) {
        with(holder.extractBinding<MediaItemBinding>()) {
            if (showThumbnails)
                when (contentItem.type) {
                    ContentType.IMAGE,
                    ContentType.VIDEO -> loadImage(contentItem.uri).into(thumbnail)
                    else -> thumbnail.setImageResource(contentItem.icon)
                }
            else
                thumbnail.setImageResource(contentItem.icon)

            title.text = contentItem.name
            contentType.setImageResource(contentItem.icon)
        }
    }

    private fun loadImage(uri: String?): RequestBuilder<*> = glide
        .load(uri)
        .override(IMAGE_WIDTH, IMAGE_HEIGHT)
        .thumbnail(0.1f)

    private fun loadDirectory(
        holder: ItemViewHolder<*>,
        contentItem: ContentItem
    ) {
        with(holder.extractBinding<DirectoryItemBinding>()) {
            title.text = contentItem.name
            thumbnail.setImageResource(R.drawable.ic_folder)
        }
    }

    inner class PreloadModelProvider : ListPreloader.PreloadModelProvider<ContentItem> {
        override fun getPreloadItems(position: Int): MutableList<ContentItem> = items
            .subList(
                (position - PRELOAD_OFFSET).coerceAtLeast(0),
                (position + PRELOAD_OFFSET).coerceAtMost(items.size - 1)
            )
            .toMutableList()

        override fun getPreloadRequestBuilder(item: ContentItem): RequestBuilder<*>? =
            loadImage(item.uri)
    }

    inner class PreloadSizeProvider :
        FixedPreloadSizeProvider<ContentItem>(IMAGE_WIDTH, IMAGE_HEIGHT)

    private fun <T : ViewBinding> ItemViewHolder<*>.extractBinding(): T =
        (this as ItemViewHolder<T>).binding

    companion object {
        private val diffCallback = DiffCallback(listOf(), listOf())

        private const val PRELOAD_OFFSET = 5
        private val IMAGE_WIDTH = 64.dp
        private val IMAGE_HEIGHT = 64.dp
    }
}

private class DiffCallback(
    oldContentItems: List<ContentItem>,
    newContentItems: List<ContentItem>
) : ItemsDiffCallback<ContentItem>(oldContentItems, newContentItems) {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldItems[oldItemPosition].uri == newItems[newItemPosition].uri
}
