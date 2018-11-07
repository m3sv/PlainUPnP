package com.m3sv.plainupnp.presentation.base

import android.content.Context
import android.widget.ArrayAdapter


class SimpleArrayAdapter<T : Any>(context: Context?, resource: Int) :
    ArrayAdapter<T>(context, resource) {

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

