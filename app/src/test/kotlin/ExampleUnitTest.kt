@file:Suppress("IllegalIdentifier")

import android.arch.lifecycle.Observer
import android.view.Display
import com.m3sv.droidupnp.presentation.main.MainActivityViewModel
import com.m3sv.droidupnp.upnp.UPnPManager
import org.droidupnp.view.DeviceDisplay
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoMoreInteractions

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {

    lateinit var upnpManager: UPnPManager

    lateinit var mainActivityViewModel: MainActivityViewModel

    @Before
    fun setup() {
        upnpManager = mock(UPnPManager::class.java)
        mainActivityViewModel = MainActivityViewModel(upnpManager)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun empty() {
        val observer: Observer<Set<DeviceDisplay>> = mock(Observer::class.java) as Observer<Set<DeviceDisplay>>
        mainActivityViewModel.renderersObservable.observeForever(observer)
        verifyNoMoreInteractions(upnpManager)

    }

    @Test
    @Throws(Exception::class)
    fun `is addition correct`() {
        assertEquals(4, (2 + 2).toLong())
    }
}