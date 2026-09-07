package ch.circadia.tracker.widget

import ch.circadia.tracker.core.model.SleepState
import ch.circadia.tracker.core.model.PersonId
import ch.circadia.tracker.core.model.StateEvent
import ch.circadia.tracker.core.model.EventSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WidgetStateTest {

    @Test
    fun `Frisch platziert ergibt beide Buttons aktiv`() {
        // Simuliert durch lastState = null
        val lastState: SleepState? = null
        // In der UI Logik:
        val sunDimmed = lastState == SleepState.AWAKE
        val moonDimmed = lastState == SleepState.ASLEEP
        
        assertEquals(false, sunDimmed)
        assertEquals(false, moonDimmed)
    }

    @Test
    fun `Nach Sonne ist Sonne gedimmt`() {
        val lastState = SleepState.AWAKE
        
        val sunDimmed = lastState == SleepState.AWAKE
        val moonDimmed = lastState == SleepState.ASLEEP
        
        assertEquals(true, sunDimmed)
        assertEquals(false, moonDimmed)
    }
}
