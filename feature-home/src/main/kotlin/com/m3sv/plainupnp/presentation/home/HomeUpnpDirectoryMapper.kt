package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.common.Mapper
import com.m3sv.plainupnp.upnp.store.UpnpDirectory
import javax.inject.Inject

class HomeUpnpDirectoryMapper @Inject constructor(private val homeContentMapper: HomeContentMapper) :
    Mapper<UpnpDirectory, Directory> {

    override fun map(input: UpnpDirectory): Directory = when (input) {
        is UpnpDirectory.Root -> Directory.Root(
            input.name,
            homeContentMapper.map(input.content)
        )

        is UpnpDirectory.SubUpnpDirectory -> Directory.SubDirectory(
            input.parentName,
            homeContentMapper.map(input.content)
        )

        is UpnpDirectory.None -> Directory.None
    }
}
