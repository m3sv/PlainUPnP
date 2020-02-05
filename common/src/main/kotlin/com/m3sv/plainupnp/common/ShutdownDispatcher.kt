package com.m3sv.plainupnp.common

// TODO switch to class and inject later
object ShutdownDispatcher {

    private val listeners: MutableSet<Shutdownable> = mutableSetOf()

    fun removeListener(shutdownable: Shutdownable) {
        if (listeners.contains(shutdownable)) {
            listeners.remove(shutdownable)
        }
    }

    fun addListener(shutdownable: Shutdownable) {
        listeners.add(shutdownable)
    }

    fun shutdown() {
        listeners.forEach(Shutdownable::shutdown)
    }
}
