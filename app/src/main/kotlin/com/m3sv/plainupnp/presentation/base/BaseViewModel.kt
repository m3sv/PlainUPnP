package com.m3sv.plainupnp.presentation.base

import androidx.lifecycle.ViewModel


abstract class BaseViewModel<Intention> : ViewModel() {

    abstract fun intention(intention: Intention)

}
