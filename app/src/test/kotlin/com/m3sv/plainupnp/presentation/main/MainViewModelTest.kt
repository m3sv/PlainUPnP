package com.m3sv.plainupnp.presentation.main

import com.m3sv.plainupnp.InstantTaskExecutorExtension
import com.m3sv.plainupnp.ShutdownNotifier
import com.m3sv.plainupnp.ShutdownNotifierImpl
import com.m3sv.plainupnp.TestObserver
import com.m3sv.plainupnp.common.Filter
import com.m3sv.plainupnp.common.FilterDelegate
import com.m3sv.plainupnp.upnp.manager.UpnpManager
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantTaskExecutorExtension::class)
internal class MainViewModelTest {

    private lateinit var upnpManager: UpnpManager
    private lateinit var volumeManager: BufferedVolumeManager
    private lateinit var filterDelegate: FilterDelegate
    private lateinit var shutdownNotifier: ShutdownNotifier
    private lateinit var deviceDisplayMapper: DeviceDisplayMapper

    private lateinit var viewModel: MainViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(TestCoroutineDispatcher())

        upnpManager = mock {
            on { upnpRendererState } doReturn flowOf()
        }

        volumeManager = mock {
            on { observeVolume() } doReturn flowOf()
        }

        filterDelegate = Filter()
        shutdownNotifier = ShutdownNotifierImpl
        deviceDisplayMapper = DeviceDisplayMapper()

        viewModel = MainViewModel(
            upnpManager = upnpManager,
            volumeManager = volumeManager,
            filterDelegate = filterDelegate,
            deviceDisplayMapper = deviceDisplayMapper,
            shutdownNotifier = shutdownNotifier
        )

    }

    @Test
    fun `given shutdown event when listening then deliver shutdown event`() {
        val observer = TestObserver<Unit>()
        viewModel.shutdown.observeForever(observer)

        (shutdownNotifier as ShutdownNotifierImpl).shutdown()

        assertTrue(observer.result.size == 1)
    }

}