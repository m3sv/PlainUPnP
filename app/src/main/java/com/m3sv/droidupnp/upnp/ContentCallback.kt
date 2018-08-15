package com.m3sv.droidupnp.upnp

import java.util.*
import java.util.concurrent.Callable


interface ContentCallback : Runnable {
    fun setContent(content: ArrayList<DIDLObjectDisplay>)
}