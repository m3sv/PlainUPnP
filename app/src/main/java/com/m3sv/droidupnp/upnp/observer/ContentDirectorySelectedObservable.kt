package com.m3sv.droidupnp.upnp.observer

import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import org.droidupnp.controller.upnp.IUPnPServiceController
import org.droidupnp.model.upnp.IUPnPDevice


class ContentDirectorySelectedObservable(private val controller: IUPnPServiceController) : Observable<IUPnPDevice>() {

    override fun subscribeActual(observer: Observer<in IUPnPDevice>) {
        val deviceObserver = ContentDeviceObserver(controller, observer)
        observer.onSubscribe(deviceObserver)
    }

    private inner class ContentDeviceObserver(private val controller: IUPnPServiceController,
                                              private val observer: Observer<in IUPnPDevice>) :
            MainThreadDisposable(), java.util.Observer {
        init {
            controller.addSelectedContentDirectoryObserver(this)
        }

        override fun onDispose() {
            controller.delSelectedContentDirectoryObserver(this)

        }

        override fun update(o: java.util.Observable?, arg: Any?) {
            val device = controller.selectedContentDirectory
            device?.let { observer.onNext(it) }
        }
    }
}