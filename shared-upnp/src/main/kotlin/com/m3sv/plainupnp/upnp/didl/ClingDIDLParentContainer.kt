package com.m3sv.plainupnp.upnp.didl

import org.fourthline.cling.support.model.container.Container

class ClingDIDLParentContainer(id: String) : ClingDIDLObject(Container()) {

    init {
        didlObject.id = id
    }

    override val title: String = ".."
}
