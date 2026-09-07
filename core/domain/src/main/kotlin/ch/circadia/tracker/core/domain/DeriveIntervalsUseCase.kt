package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.common.Clock
import ch.circadia.tracker.core.model.Interval
import ch.circadia.tracker.core.model.StateEvent

class DeriveIntervalsUseCase(private val clock: Clock) {
    operator fun invoke(events: List<StateEvent>, windowEndUtc: Long = clock.nowEpochMillis()): List<Interval> {
        val activeEvents = events
            .filter { it.voidedAtUtcMillis == null }
            .sortedWith(
                compareBy<StateEvent> { it.occurredAtUtcMillis }
                    .thenBy { it.recordedAtUtcMillis }
            )

        if (activeEvents.isEmpty()) return emptyList()

        val intervals = mutableListOf<Interval>()
        var currentEvent = activeEvents.first()

        for (i in 1 until activeEvents.size) {
            val nextEvent = activeEvents[i]
            if (nextEvent.state != currentEvent.state) {
                intervals.add(
                    Interval(
                        personId = currentEvent.personId,
                        state = currentEvent.state,
                        startUtcMillis = currentEvent.occurredAtUtcMillis,
                        endUtcMillis = nextEvent.occurredAtUtcMillis,
                        isOpen = false,
                        startZoneId = currentEvent.timeZoneId,
                        endZoneId = nextEvent.timeZoneId,
                        startEventId = currentEvent.id
                    )
                )
                currentEvent = nextEvent
            }
        }

        intervals.add(
            Interval(
                personId = currentEvent.personId,
                state = currentEvent.state,
                startUtcMillis = currentEvent.occurredAtUtcMillis,
                endUtcMillis = windowEndUtc,
                isOpen = true,
                startZoneId = currentEvent.timeZoneId,
                endZoneId = currentEvent.timeZoneId,
                startEventId = currentEvent.id
            )
        )

        return intervals
    }
}
