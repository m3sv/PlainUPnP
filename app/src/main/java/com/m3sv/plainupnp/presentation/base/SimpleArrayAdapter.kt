package com.m3sv.plainupnp.presentation.base

import android.content.Context
import android.widget.ArrayAdapter


class SimpleArrayAdapter<T : Any>(context: Context) :
    ArrayAdapter<T>(context, android.R.layout.simple_list_item_1) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    var items: List<T> = emptyList()
        private set

    fun setNewItems(items: List<T>) {
        if (this.items != items) {
            this.items = items
            clear()
            addAll(items)
            notifyDataSetChanged()
        }
    }
}

