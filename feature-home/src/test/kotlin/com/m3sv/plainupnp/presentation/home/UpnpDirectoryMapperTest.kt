package com.m3sv.plainupnp.presentation.home

import com.m3sv.plainupnp.upnp.didl.ClingDIDLObject
import com.m3sv.plainupnp.upnp.store.UpnpDirectory
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class UpnpDirectoryMapperTest {

    private lateinit var upnpDirectoryMapper: UpnpDirectoryMapper

    private val contentMapper = mock<ClingContentMapper>()

    private val name = "name"

    private val contents = listOf<ClingDIDLObject>(
        mock(),
        mock(),
        mock()
    )

    private val mappedContents = listOf<ContentItem>(
        mock(),
        mock(),
        mock()
    )

    @BeforeEach
    internal fun setUp() {
        whenever(contentMapper.map(contents)).thenReturn(mappedContents)
        upnpDirectoryMapper = UpnpDirectoryMapper(contentMapper)
    }

    @Test
    fun `given root directory when map then return Root UpnpFolder`() {
        val input = UpnpDirectory.Root(name, contents)

        val expected = UpnpFolder.Root(name, mappedContents)

        assertEquals(expected, upnpDirectoryMapper.map(input))
    }

    @Test
    fun `given root directory when map then return Upnp SubFolder`() {
        val input = UpnpDirectory.SubDirectory(name, contents)

        val expected = UpnpFolder.SubFolder(name, mappedContents)

        assertEquals(expected, upnpDirectoryMapper.map(input))
    }

    @Test
    fun `given none directory when map then return none folder`() {
        val input = UpnpDirectory.None

        val expected = UpnpFolder.None

        assertEquals(expected, upnpDirectoryMapper.map(input))
    }
}
