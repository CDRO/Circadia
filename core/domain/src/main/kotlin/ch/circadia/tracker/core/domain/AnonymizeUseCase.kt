package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class AnonymizeUseCase {

    fun anonymizeEvents(
        events: List<StateEvent>,
        pseudonym: String,
        studyStartUtcMillis: Long
    ): List<AnonymizedEvent> {
        return events.map { event ->
            val occurredAt = Instant.ofEpochMilli(event.occurredAtUtcMillis)
            val roundedInstant = occurredAt.truncatedTo(ChronoUnit.MINUTES).let {
                val minutes = (it.toEpochMilli() / 60000)
                val roundedMinutes = (minutes / 5) * 5
                Instant.ofEpochMilli(roundedMinutes * 60000)
            }
            
            val zdt = occurredAt.atZone(ZoneId.of(event.timeZoneId))
            val offsetSeconds = zdt.offset.totalSeconds
            
            val dayOffset = ChronoUnit.DAYS.between(
                Instant.ofEpochMilli(studyStartUtcMillis).atZone(ZoneId.of("UTC")).toLocalDate(),
                zdt.toLocalDate()
            )

            AnonymizedEvent(
                pseudonym = pseudonym,
                state = event.state,
                roundedOccurredAtUtcMillis = roundedInstant.toEpochMilli(),
                dayOfWeek = zdt.dayOfWeek.value,
                dayOffset = dayOffset,
                utcOffsetSeconds = offsetSeconds
            )
        }
    }
}

data class AnonymizedEvent(
    val pseudonym: String,
    val state: SleepState,
    val roundedOccurredAtUtcMillis: Long,
    val dayOfWeek: Int,
    val dayOffset: Long,
    val utcOffsetSeconds: Int
)
