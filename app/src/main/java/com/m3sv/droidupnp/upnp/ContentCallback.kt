package com.m3sv.droidupnp.upnp

import java.util.*


interface ContentCallback : Runnable {
    fun setContent(content: ArrayList<DIDLObjectDisplay>)
}