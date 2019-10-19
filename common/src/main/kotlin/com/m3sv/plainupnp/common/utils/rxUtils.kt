package com.m3sv.plainupnp.common.utils

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.disposeBy(disposable: CompositeDisposable) {
    disposable.add(this)
}