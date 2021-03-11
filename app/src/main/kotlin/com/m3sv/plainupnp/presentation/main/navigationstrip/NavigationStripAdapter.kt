package com.m3sv.plainupnp.presentation.main.navigationstrip

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.upnp.folder.Folder

class NavigationStripAdapter(private val clickListener: (Folder) -> Unit) :
    ListAdapter<Folder, NavigationStripViewHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavigationStripViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.navigation_strip_item, parent, false) as TextView

        return NavigationStripViewHolder(view)
    }

    override fun onBindViewHolder(holder: NavigationStripViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }
}

class NavigationStripViewHolder(itemView: TextView) : RecyclerView.ViewHolder(itemView) {

    fun bind(folder: Folder, clickListener: (Folder) -> Unit) {
        with(itemView as TextView) {
            text = folder.title
            setOnClickListener {
                clickListener(folder)
            }
        }
    }
}


object Differ : DiffUtil.ItemCallback<Folder>() {
    override fun areItemsTheSame(
        oldItem: Folder,
        newItem: Folder,
    ): Boolean = oldItem === newItem

    override fun areContentsTheSame(oldItem: Folder, newItem: Folder): Boolean =
        oldItem == newItem
}
