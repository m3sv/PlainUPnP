package com.m3sv.plainupnp.presentation.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.m3sv.plainupnp.data.upnp.DeviceDisplay
import com.m3sv.plainupnp.data.upnp.DeviceType
import com.m3sv.plainupnp.data.upnp.Directory
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Observable
import org.droidupnp.legacy.cling.CDevice
import org.junit.Before
import org.junit.Test
import org.junit.Rule



class MainActivityViewModelTest {

    @Rule
    @JvmField
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: MainActivityViewModel

    private val defaultDevice = CDevice(mock())

    private val renderers = setOf(
            DeviceDisplay(defaultDevice, type = DeviceType.PLAY_LOCALLY),
            DeviceDisplay(defaultDevice, type = DeviceType.RENDERER))

    private val contentDirectories = setOf(
            DeviceDisplay(defaultDevice, type = DeviceType.PLAY_LOCALLY),
            DeviceDisplay(defaultDevice, type = DeviceType.CONTENT_DIRECTORY))

    @Before
    fun setUp() {
        viewModel = MainActivityViewModel(mock {
            on { selectedDirectoryObservable } doReturn Observable.just(Directory.Home("home"), Directory.SubDirectory("0", "1", null))
            on { rendererDiscovery } doReturn Observable.just(renderers)
            on { contentDirectoryDiscovery } doReturn Observable.just(contentDirectories)
        })
    }

    @Test
    fun `renderers delivered to view`() {
        val mockObserver = mock<Observer<Set<DeviceDisplay>>>()
        viewModel.renderers.observeForever(mockObserver)
        verify(mockObserver).onChanged(renderers)
    }

    @Test
    fun `content directories delivered to view`() {
        val mockObserver = mock<Observer<Set<DeviceDisplay>>>()
        viewModel.contentDirectories.observeForever(mockObserver)
        verify(mockObserver).onChanged(contentDirectories)
    }
}