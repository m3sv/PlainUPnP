package com.m3sv.droidupnp.presentation.base

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater

open class BaseViewHolder<out T : ViewDataBinding>(val binding: T) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun <T : ViewDataBinding> createViewHolder(
            layoutInflater: LayoutInflater,
            clazz: Class<T>
        ): BaseViewHolder<T> {
            return BaseViewHolder(
                clazz.getMethod(
                    "inflate",
                    LayoutInflater::class.java
                ).invoke(null, layoutInflater) as T
            )
        }
    }
}