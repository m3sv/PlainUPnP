package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.common.Mapper
import com.m3sv.plainupnp.upnp.store.UpnpDirectory
import javax.inject.Inject

class UpnpDirectoryMapper @Inject constructor(private val clingContentMapper: ClingContentMapper) :
    Mapper<UpnpDirectory, UpnpFolder> {

    override fun map(input: UpnpDirectory): UpnpFolder = when (input) {
        is UpnpDirectory.Root -> UpnpFolder.Root(
            input.name,
            clingContentMapper.map(input.content)
        )

        is UpnpDirectory.SubDirectory -> UpnpFolder.SubFolder(
            input.parentName,
            clingContentMapper.map(input.content)
        )

        is UpnpDirectory.None -> UpnpFolder.None
    }
}
