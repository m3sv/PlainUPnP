package com.m3sv.plainupnp.presentation.base

import android.content.Context
import android.widget.ArrayAdapter


class SimpleArrayAdapter<T : Any>(context: Context?) :
    ArrayAdapter<T>(context, android.R.layout.simple_list_item_1) {

    init {
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    private var items: List<T> = emptyList()

    fun setNewItems(items: List<T>) {
        if (this.items != items) {
            this.items = items
            clear()
            addAll(items)
        }
    }
}

