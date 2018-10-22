package com.m3sv.plainupnp.upnp

import java.util.*


interface ContentCallback : Runnable {
    fun setContent(content: ArrayList<DIDLObjectDisplay>)
}