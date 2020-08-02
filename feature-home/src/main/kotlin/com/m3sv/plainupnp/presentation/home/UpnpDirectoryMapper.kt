package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.common.Mapper
import com.m3sv.plainupnp.upnp.store.UpnpDirectory
import javax.inject.Inject

class UpnpDirectoryMapper @Inject constructor(private val homeContentMapper: HomeContentMapper) :
    Mapper<UpnpDirectory, UpnpFolder> {

    override fun map(input: UpnpDirectory): UpnpFolder = when (input) {
        is UpnpDirectory.Root -> UpnpFolder.Root(
            input.name,
            homeContentMapper.map(input.content)
        )

        is UpnpDirectory.SubUpnpDirectory -> UpnpFolder.SubFolder(
            input.parentName,
            homeContentMapper.map(input.content)
        )

        is UpnpDirectory.None -> UpnpFolder.None
    }
}
