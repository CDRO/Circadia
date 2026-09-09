package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.Instant

class AnonymizeUseCaseTest {
    private val useCase = AnonymizeUseCase()

    @Test
    fun `Events werden auf 5 Minuten gerundet`() {
        val event = StateEvent(
            id = "e1",
            personId = PersonId("p1"),
            state = SleepState.ASLEEP,
            occurredAtUtcMillis = Instant.parse("2024-09-07T10:47:30Z").toEpochMilli(),
            timeZoneId = "UTC",
            source = EventSource.APP,
            recordedAtUtcMillis = 0L
        )
        
        val result = useCase.anonymizeEvents(listOf(event), "anon-1", 0L)
        val expected = Instant.parse("2024-09-07T10:45:00Z").toEpochMilli()
        
        assertEquals(expected, result[0].roundedOccurredAtUtcMillis)
    }

    @Test
    fun `PII wie Name oder PersonId kommt nicht im Ergebnis vor`() {
        val event = StateEvent(
            id = "e1",
            personId = PersonId("p1"),
            state = SleepState.ASLEEP,
            occurredAtUtcMillis = 0L,
            timeZoneId = "UTC",
            source = EventSource.APP,
            recordedAtUtcMillis = 0L
        )
        
        val result = useCase.anonymizeEvents(listOf(event), "anon-1", 0L)
        
        // This is a bit redundant as the data class doesn't have these fields,
        // but good to have as a principle check.
        assertFalse(result[0].toString().contains("p1"))
        assertFalse(result[0].toString().contains("e1"))
        assertEquals("anon-1", result[0].pseudonym)
    }
}
