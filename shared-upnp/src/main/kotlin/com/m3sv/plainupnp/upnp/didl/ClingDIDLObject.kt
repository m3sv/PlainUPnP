package com.m3sv.plainupnp.upnp.didl

import org.fourthline.cling.support.model.DIDLObject


open class ClingDIDLObject internal constructor(val didlObject: DIDLObject) :
    com.m3sv.plainupnp.data.upnp.DIDLObject {

    override val title: String = didlObject.title

    override val id: String = didlObject.id
}
