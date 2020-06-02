package com.m3sv.plainupnp.presentation.base

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.widget.ArrayAdapter
import android.widget.Filter
import com.m3sv.plainupnp.common.StatefulComponent


class SimpleArrayAdapter<T : Parcelable> constructor(
    context: Context,
    private val key: String
) : ArrayAdapter<T>(context, android.R.layout.simple_list_item_1), StatefulComponent {

    private var items: ArrayList<out T> = ArrayList()

    fun setNewItems(items: List<T>) {
        if (this.items != items) {
            this.items = ArrayList(items)
            clear()
            addAll(items)
            notifyDataSetChanged()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(key, items)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val restoredItems: List<T> = savedInstanceState.getParcelableArrayList(key) ?: listOf()
        setNewItems(restoredItems)
    }

    companion object {
        inline fun <reified T : Parcelable> init(
            context: Context,
            key: String
        ): SimpleArrayAdapter<T> = SimpleArrayAdapter(context, key)
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults? = null

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        }
    }
}

class SpinnerItem(private val name: String) : Parcelable {

    override fun toString(): String = name

    constructor(parcel: Parcel) : this(requireNotNull(parcel.readString()))

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<SpinnerItem> {
        override fun createFromParcel(parcel: Parcel): SpinnerItem {
            return SpinnerItem(parcel)
        }

        override fun newArray(size: Int): Array<SpinnerItem?> {
            return arrayOfNulls(size)
        }
    }
}
