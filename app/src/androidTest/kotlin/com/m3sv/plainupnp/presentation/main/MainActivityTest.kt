package com.m3sv.plainupnp.presentation.main


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withSpinnerText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import com.m3sv.plainupnp.R
import com.m3sv.plainupnp.RecyclerViewItemCountAssertion
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var grantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.READ_EXTERNAL_STORAGE")

    @Test
    fun mainActivityTest() {
        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        Thread.sleep(2000)

        onView(withId(R.id.main_content_device_picker)).check(matches(withSpinnerText(containsString("Android SDK built for x86"))))
        onView(withId(R.id.main_renderer_device_picker)).check(matches(withSpinnerText(containsString("Play locally"))))

        onView(withId(R.id.content)).check(RecyclerViewItemCountAssertion(3))
    }
}
