package com.m3sv.plainupnp.data.upnp

interface DIDLObject {
    val dataType: String
    val title: String
    val description: String
    val icon: Int
    val parentID: String
    val id: String
}
