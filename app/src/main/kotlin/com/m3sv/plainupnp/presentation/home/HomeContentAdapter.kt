package com.m3sv.plainupnp.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.common.ItemsDiffCallback
import com.m3sv.plainupnp.databinding.FolderItemBinding
import com.m3sv.plainupnp.databinding.MediaItemBinding
import com.m3sv.plainupnp.presentation.base.BaseAdapter
import com.m3sv.plainupnp.presentation.base.ItemViewHolder
import java.util.*


typealias OnItemClickListener = (Int) -> Unit

class GalleryContentAdapter(
    private val glide: RequestManager,
    private val onItemClickListener: OnItemClickListener
) : BaseAdapter<ContentItem>(diffCallback) {

    // TODO update using OnSharePreferencesChangedListener
    var loadThumbnails = true

    private val emptyRequestOptions = RequestOptions()

    override fun getItemViewType(position: Int): Int = items[position].type.ordinal

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder<ViewDataBinding> = when (ContentType.values()[viewType]) {
        ContentType.DIRECTORY -> ItemViewHolder(
            FolderItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), onItemClickListener
        )

        else -> ItemViewHolder(
            MediaItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), onItemClickListener
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder<ViewDataBinding>, position: Int) {
        val item = items[position]

        holder.bind(item)

        when (item.type) {
            ContentType.DIRECTORY -> loadDirectory(holder, item)
            else -> loadData(holder, item)
        }
    }

    private fun loadData(
        holder: ItemViewHolder<ViewDataBinding>,
        contentItem: ContentItem,
        requestOptions: RequestOptions = emptyRequestOptions
    ) {

        with(holder.extractBinding<MediaItemBinding>()) {
            if (loadThumbnails)
                when (contentItem.type) {
                    ContentType.IMAGE,
                    ContentType.VIDEO -> glide.load(contentItem.thumbnailUri)
                        .thumbnail(0.1f)
                        .apply(requestOptions)
                        .into(thumbnail)
                    else -> thumbnail.setImageResource(contentItem.icon)
                }
            else
                thumbnail.setImageResource(contentItem.icon)

            title.text = contentItem.name
            contentType.setImageResource(contentItem.icon)
        }
    }

    private fun loadDirectory(
        holder: ItemViewHolder<*>,
        contentItem: ContentItem
    ) {
        with(holder.extractBinding<FolderItemBinding>()) {
            title.text = contentItem.name
            thumbnail.setImageResource(R.drawable.ic_folder)
        }
    }

    fun filter(text: CharSequence) {
        if (text.isEmpty()) {
            resetItems()
            return
        }

        filterWithDiff { it.name.toLowerCase(Locale.getDefault()).contains(text) }
    }

    class DiffCallback(
        oldContentItems: List<ContentItem>,
        newContentItems: List<ContentItem>
    ) : ItemsDiffCallback<ContentItem>(oldContentItems, newContentItems) {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldItems[oldItemPosition].uri == newItems[newItemPosition].uri
    }

    private fun <T : ViewDataBinding> ItemViewHolder<*>.extractBinding(): T =
        (this as ItemViewHolder<T>).binding

    companion object {
        val diffCallback = DiffCallback(listOf(), listOf())
    }
}
