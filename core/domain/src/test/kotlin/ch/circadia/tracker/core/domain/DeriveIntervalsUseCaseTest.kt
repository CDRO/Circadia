package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.common.Clock
import ch.circadia.tracker.core.model.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class DeriveIntervalsUseCaseTest {

    private val fakeClock = object : Clock {
        var now = 0L
        override fun nowEpochMillis() = now
    }
    private val useCase = DeriveIntervalsUseCase(fakeClock)
    private val personId = PersonId("p1")

    @Test
    fun `leere Eventliste ergibt leeres Ergebnis`() {
        val result = useCase(emptyList(), 1000L)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `einzelnes Event ergibt offenes Intervall`() {
        val events = listOf(
            createEvent("e1", SleepState.ASLEEP, 500L)
        )
        val result = useCase(events, 1000L)
        assertEquals(1, result.size)
        with(result[0]) {
            assertEquals(SleepState.ASLEEP, state)
            assertEquals(500L, startUtcMillis)
            assertEquals(1000L, endUtcMillis)
            assertTrue(isOpen)
        }
    }

    @Test
    fun `zwei gleiche Zustaende ergeben ein Intervall`() {
        val events = listOf(
            createEvent("e1", SleepState.ASLEEP, 500L),
            createEvent("e2", SleepState.ASLEEP, 700L)
        )
        val result = useCase(events, 1000L)
        assertEquals(1, result.size)
        assertEquals(500L, result[0].startUtcMillis)
        assertEquals(1000L, result[0].endUtcMillis)
    }

    @Test
    fun `rueckdatiertes Event wird korrekt einsortiert`() {
        val events = listOf(
            createEvent("e2", SleepState.AWAKE, 800L),
            createEvent("e1", SleepState.ASLEEP, 500L)
        )
        val result = useCase(events, 1000L)
        assertEquals(2, result.size)
        assertEquals(500L, result[0].startUtcMillis)
        assertEquals(800L, result[0].endUtcMillis)
    }

    @Test
    fun `voidedes Event wird ignoriert`() {
        val events = listOf(
            createEvent("e1", SleepState.ASLEEP, 500L, voidedAt = 600L),
            createEvent("e2", SleepState.AWAKE, 800L)
        )
        val result = useCase(events, 1000L)
        assertEquals(1, result.size)
        assertEquals(800L, result[0].startUtcMillis)
    }

    @Test
    fun `Intervall ueber Mitternacht bleibt zusammen`() {
        // 2024-09-07 23:00 -> 2024-09-08 07:00
        val start = ZonedDateTime.of(2024, 9, 7, 23, 0, 0, 0, ZoneId.of("UTC")).toInstant().toEpochMilli()
        val end = ZonedDateTime.of(2024, 9, 8, 7, 0, 0, 0, ZoneId.of("UTC")).toInstant().toEpochMilli()
        
        val events = listOf(createEvent("e1", SleepState.ASLEEP, start))
        val result = useCase(events, end)
        
        assertEquals(1, result.size)
        assertEquals(start, result[0].startUtcMillis)
        assertEquals(end, result[0].endUtcMillis)
    }

    @Test
    fun `Intervall ueber Sommerzeitumstellung behaelt echte Dauer`() {
        // Europe/Zurich: 2024-03-31 02:00 -> 03:00
        val zone = ZoneId.of("Europe/Zurich")
        val startLocal = ZonedDateTime.of(2024, 3, 31, 1, 0, 0, 0, zone) // 00:00 UTC
        val endLocal = ZonedDateTime.of(2024, 3, 31, 4, 0, 0, 0, zone)   // 02:00 UTC
        
        val events = listOf(createEvent("e1", SleepState.ASLEEP, startLocal.toInstant().toEpochMilli(), zoneId = "Europe/Zurich"))
        val result = useCase(events, endLocal.toInstant().toEpochMilli())
        
        assertEquals(1, result.size)
        // Dauer sollte 3 Stunden lokal sein, aber 2 Stunden real (wegen Spring Forward)
        val durationMillis = result[0].endUtcMillis - result[0].startUtcMillis
        assertEquals(2 * 3600 * 1000L, durationMillis)
    }

    @Test
    fun `Intervall ueber Winterzeitumstellung behaelt echte Dauer`() {
        // Europe/Zurich: 2024-10-27 03:00 -> 02:00
        val zone = ZoneId.of("Europe/Zurich")
        val startLocal = ZonedDateTime.of(2024, 10, 27, 1, 0, 0, 0, zone) // 23:00 UTC (prev day)
        val endLocal = ZonedDateTime.of(2024, 10, 27, 4, 0, 0, 0, zone)   // 03:00 UTC
        
        val events = listOf(createEvent("e1", SleepState.ASLEEP, startLocal.toInstant().toEpochMilli(), zoneId = "Europe/Zurich"))
        val result = useCase(events, endLocal.toInstant().toEpochMilli())
        
        assertEquals(1, result.size)
        // Dauer sollte 3 Stunden lokal sein, aber 4 Stunden real (wegen Fall Back)
        val durationMillis = result[0].endUtcMillis - result[0].startUtcMillis
        assertEquals(4 * 3600 * 1000L, durationMillis)
    }

    @Test
    fun `Korrektur ersetzt Original ohne es zu loeschen`() {
        // In der Domain-Logik werden Korrekturen durch supersedesEventId im Event-Log abgebildet.
        // Das Repository filtert normalerweise alte Events, aber der UseCase sortiert sie einfach.
        // Wenn das Repo nur die neuesten Events liefert, funktioniert es.
        // Wenn der UseCase alle bekommt, muss er die Ersetzungen beachten.
        // Spec #a-5-3 Rule 6: "Korrektur ersetzt Original..." 
        // Eigentlich filtert das Repo voided events. Korrekturen voiden das Original.
        val events = listOf(
            createEvent("e1", SleepState.ASLEEP, 500L, voidedAt = 600L),
            createEvent("e2", SleepState.ASLEEP, 550L, supersedesId = "e1")
        )
        val result = useCase(events, 1000L)
        assertEquals(1, result.size)
        assertEquals(550L, result[0].startUtcMillis)
    }

    @Test
    fun `Reise über Zeitzonen zwischen zwei Events behaelt echte Dauer`() {
        // Start in Zürich (UTC+1), Ende in New York (UTC-5)
        val start = ZonedDateTime.of(2024, 9, 7, 10, 0, 0, 0, ZoneId.of("Europe/Zurich")) // 09:00 UTC
        val end = ZonedDateTime.of(2024, 9, 7, 15, 0, 0, 0, ZoneId.of("America/New_York")) // 19:00 UTC
        
        val events = listOf(
            createEvent("e1", SleepState.ASLEEP, start.toInstant().toEpochMilli(), zoneId = "Europe/Zurich"),
            createEvent("e2", SleepState.AWAKE, end.toInstant().toEpochMilli(), zoneId = "America/New_York")
        )
        val result = useCase(events, end.toInstant().toEpochMilli() + 1000)
        
        assertEquals(2, result.size)
        assertEquals(11 * 3600 * 1000L, result[0].endUtcMillis - result[0].startUtcMillis)
        assertEquals("Europe/Zurich", result[0].startZoneId)
        assertEquals("America/New_York", result[0].endZoneId)
    }

    @Test
    fun `Zwei Events in derselben Minute haben deterministische Reihenfolge`() {
        val events = listOf(
            createEvent("e2", SleepState.AWAKE, 500L).copy(recordedAtUtcMillis = 550L),
            createEvent("e1", SleepState.ASLEEP, 500L).copy(recordedAtUtcMillis = 510L)
        )
        val result = useCase(events, 1000L)
        assertEquals(2, result.size)
        assertEquals(SleepState.ASLEEP, result[0].state)
        assertEquals(SleepState.AWAKE, result[1].state)
    }

    private fun createEvent(
        id: String,
        state: SleepState,
        occurredAt: Long,
        zoneId: String = "UTC",
        voidedAt: Long? = null,
        supersedesId: String? = null
    ) = StateEvent(
        id = id,
        personId = personId,
        state = state,
        occurredAtUtcMillis = occurredAt,
        timeZoneId = zoneId,
        source = EventSource.APP,
        recordedAtUtcMillis = occurredAt,
        voidedAtUtcMillis = voidedAt,
        supersedesEventId = supersedesId
    )
}
