package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.common.Clock
import ch.circadia.tracker.core.model.*
import java.util.UUID
import kotlin.math.abs

class RecordStateEventUseCase(
    private val eventRepository: StateEventRepository,
    private val clock: Clock
) {
    suspend operator fun invoke(
        personId: PersonId,
        state: SleepState,
        source: EventSource,
        occurredAtUtcMillis: Long? = null,
        timeZoneId: String? = null,
        note: String? = null
    ) {
        val now = clock.nowEpochMillis()
        val rawOccurredAt = occurredAtUtcMillis ?: now
        
        // Idempotency: same state, same person, within 2 seconds (physical interaction)
        val latest = eventRepository.getLatestEvent(personId)
        if (latest != null && 
            latest.state == state && 
            latest.voidedAtUtcMillis == null &&
            abs(latest.recordedAtUtcMillis - now) <= 2_000
        ) {
            return
        }

        // Round to full minute: (ms / 60000) * 60000
        val roundedOccurredAt = (rawOccurredAt / 60_000) * 60_000
        
        val zoneId = timeZoneId ?: java.time.ZoneId.systemDefault().id

        val event = StateEvent(
            id = UUID.randomUUID().toString(),
            personId = personId,
            state = state,
            occurredAtUtcMillis = roundedOccurredAt,
            timeZoneId = zoneId,
            source = source,
            recordedAtUtcMillis = now,
            note = note
        )
        
        eventRepository.addEvent(event)
    }
}
