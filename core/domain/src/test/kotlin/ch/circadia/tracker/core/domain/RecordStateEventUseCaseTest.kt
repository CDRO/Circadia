package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.common.Clock
import ch.circadia.tracker.core.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RecordStateEventUseCaseTest {

    private val fakeClock = object : Clock {
        var now = 0L
        override fun nowEpochMillis() = now
    }

    private val fakeRepo = object : StateEventRepository {
        val events = mutableListOf<StateEvent>()
        override fun getEvents(personId: PersonId): Flow<List<StateEvent>> = emptyFlow()
        override suspend fun getLatestEvent(personId: PersonId): StateEvent? = events.lastOrNull { it.personId == personId }
        override suspend fun addEvent(event: StateEvent) { events.add(event) }
        override suspend fun voidEvent(eventId: String, voidedAt: Long) {}
        override fun getEventChain(eventId: String): Flow<List<StateEvent>> = emptyFlow()
    }

    private val useCase = RecordStateEventUseCase(fakeRepo, fakeClock)
    private val personId = PersonId("p1")

    @Test
    fun `Zeitstempel wird auf volle Minute abgerundet`() = runTest {
        // 2024-09-07 10:45:30 -> 10:45:00
        val input = 1725705930000L 
        val expected = 1725705900000L
        
        useCase(personId, SleepState.ASLEEP, EventSource.WIDGET, input)
        
        assertEquals(expected, fakeRepo.events.last().occurredAtUtcMillis)
    }

    @Test
    fun `Doppeltipp innerhalb von 2 Sekunden wird ignoriert`() = runTest {
        val time = 100_000L
        fakeClock.now = time
        
        // Erster Tipp
        useCase(personId, SleepState.ASLEEP, EventSource.WIDGET, time)
        assertEquals(1, fakeRepo.events.size)
        
        // Zweiter Tipp 1,5s später (physisch)
        fakeClock.now = time + 1500L
        useCase(personId, SleepState.ASLEEP, EventSource.WIDGET, time + 1500L)
        assertEquals(1, fakeRepo.events.size)
    }

    @Test
    fun `Zweiter Tipp mit anderem Status wird NICHT ignoriert`() = runTest {
        val time = 100_000L
        fakeClock.now = time
        
        useCase(personId, SleepState.ASLEEP, EventSource.WIDGET, time)
        
        fakeClock.now = time + 500L
        useCase(personId, SleepState.AWAKE, EventSource.WIDGET, time + 500L)
        
        assertEquals(2, fakeRepo.events.size)
    }

    @Test
    fun `Ereignis nach mehr als 2 Sekunden wird NICHT ignoriert`() = runTest {
        val time = 100_000L
        fakeClock.now = time
        
        useCase(personId, SleepState.ASLEEP, EventSource.WIDGET, time)
        
        // 3 Sekunden später (physisch)
        fakeClock.now = time + 3000L
        useCase(personId, SleepState.ASLEEP, EventSource.WIDGET, time + 3000L)
        
        assertEquals(2, fakeRepo.events.size)
    }

    @Test
    fun `Parameter werden korrekt an Repository uebergeben`() = runTest {
        fakeClock.now = 5000L
        useCase(personId, SleepState.ASLEEP, EventSource.APP, 60_000L, "Europe/Berlin", "Test Note")
        
        val event = fakeRepo.events.last()
        assertEquals(personId, event.personId)
        assertEquals(SleepState.ASLEEP, event.state)
        assertEquals(EventSource.APP, event.source)
        assertEquals(60_000L, event.occurredAtUtcMillis)
        assertEquals("Europe/Berlin", event.timeZoneId)
        assertEquals(5000L, event.recordedAtUtcMillis)
        assertEquals("Test Note", event.note)
    }
}
