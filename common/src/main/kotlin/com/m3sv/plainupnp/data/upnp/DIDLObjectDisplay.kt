package com.m3sv.plainupnp.data.upnp


data class DIDLObjectDisplay(val didlObject: DIDLObject) {

    val title: String = didlObject.title

    val icon: Int = didlObject.icon

}
