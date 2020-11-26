package com.m3sv.plainupnp.upnp.didl

import org.fourthline.cling.support.model.DIDLObject


open class ClingDIDLObject internal constructor(val didlObject: DIDLObject) {
    open val title: String = didlObject.title
    open val uri: String? = didlObject.firstResource?.value
    val id: String = didlObject.id
}
