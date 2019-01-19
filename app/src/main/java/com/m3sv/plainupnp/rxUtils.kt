package com.m3sv.plainupnp

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable


fun Disposable.disposeBy(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}