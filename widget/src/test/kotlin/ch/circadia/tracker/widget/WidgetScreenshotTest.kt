package ch.circadia.tracker.widget

import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class WidgetScreenshotTest {

    @Test
    fun captureWidget() = runGlanceAppWidgetUnitTest {
        // Just verify it doesn't crash for now
    }
}
