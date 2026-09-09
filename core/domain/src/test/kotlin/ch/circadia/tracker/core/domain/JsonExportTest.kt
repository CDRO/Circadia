package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class JsonExportTest {
    private val useCase = JsonExportUseCase()

    @Test
    fun `JSON Export und Import Roundtrip ist verlustfrei`() {
        val person = Person(
            id = PersonId("p1"),
            displayName = "Lena",
            colorSeed = 123,
            sortIndex = 0,
            createdAtUtcMillis = 1000L
        )
        val event = StateEvent(
            id = "e1",
            personId = PersonId("p1"),
            state = SleepState.ASLEEP,
            occurredAtUtcMillis = 2000L,
            timeZoneId = "Europe/Zurich",
            source = EventSource.WIDGET,
            recordedAtUtcMillis = 3000L,
            note = "Test Note"
        )
        
        val json = useCase.exportToJson(listOf(person), listOf(event))
        val imported = useCase.importFromJson(json)
        
        assertEquals(1, imported.schemaVersion)
        assertEquals(listOf(person), imported.persons)
        assertEquals(listOf(event), imported.events)
    }
}
