package com.m3sv.plainupnp

import androidx.lifecycle.Observer

class TestObserver<T> : Observer<T> {
    private val items: MutableList<T> = mutableListOf()

    val result: List<T> = items

    override fun onChanged(t: T) {
        items.add(t)
    }
}
