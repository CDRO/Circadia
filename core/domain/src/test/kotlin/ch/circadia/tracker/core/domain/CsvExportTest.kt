package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CsvExportTest {
    private val useCase = CsvExportUseCase()

    @Test
    fun `Export bei 0 Events liefert Header`() {
        val result = useCase.exportEvents(emptyList())
        val lines = result.removePrefix("\uFEFF").trim().split("\n")
        
        assertEquals(1, lines.size)
        assertTrue(lines[0].startsWith("id,personId"))
    }

    @Test
    fun `CSV mit Sonderzeichen korrekt maskiert`() {
        val event = StateEvent(
            id = "e1",
            personId = PersonId("p1"),
            state = SleepState.ASLEEP,
            occurredAtUtcMillis = 1000L,
            timeZoneId = "UTC",
            source = EventSource.APP,
            recordedAtUtcMillis = 2000L,
            note = "Note with , and \"quote\" and \nnewline"
        )
        
        val result = useCase.exportEvents(listOf(event))
        val content = result.removePrefix("\uFEFF")
        
        // Check if note is properly quoted and escaped
        assertTrue(content.contains("\"Note with , and \"\"quote\"\" and \nnewline\""))
        
        // Count entries by counting quotes (simplified check)
        // Each quoted field has at least two quotes. Our note has escaped quotes too.
        // Let's just check the starting part of the row
        assertTrue(content.contains("e1,p1,ASLEEP,1000,UTC,APP,2000,,,"))
    }
}
