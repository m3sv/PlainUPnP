package com.m3sv.plainupnp.presentation.base

import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView

open class ItemViewHolder<out T : ViewDataBinding>(val binding: T) :
    RecyclerView.ViewHolder(binding.root)