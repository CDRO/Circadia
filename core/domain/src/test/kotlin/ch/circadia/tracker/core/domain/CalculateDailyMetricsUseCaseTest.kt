package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.Interval
import ch.circadia.tracker.core.model.PersonId
import ch.circadia.tracker.core.model.SleepState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CalculateDailyMetricsUseCaseTest {
    private val useCase = CalculateDailyMetricsUseCase()

    @Test
    fun `Empty intervals return null`() {
        val result = useCase(emptyList(), 0L, 1000L)
        assertEquals(null, result)
    }

    @Test
    fun `Calculates total sleep correctly within bucket`() {
        val personId = PersonId("1")
        val intervals = listOf(
            Interval(personId, SleepState.ASLEEP, 100L, 500L, false, "UTC", "UTC", "e1"),
            Interval(personId, SleepState.AWAKE, 500L, 600L, false, "UTC", "UTC", "e2"),
            Interval(personId, SleepState.ASLEEP, 600L, 900L, false, "UTC", "UTC", "e3")
        )
        
        val result = useCase(intervals, 0L, 1000L)
        
        assertEquals(400L + 300L, result?.totalSleepMillis)
        assertEquals(2, result?.sleepEpisodes)
        assertEquals(100L, result?.maxAwakeMillis)
    }
}
