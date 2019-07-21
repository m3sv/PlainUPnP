package com.m3sv.plainupnp.data.upnp


data class DIDLObjectDisplay(val didlObject: DIDLObject) {

    val title: String = didlObject.title

    val description: String = didlObject.description

    val count: String = didlObject.count

    val icon: Int = didlObject.icon
}
