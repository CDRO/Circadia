package ch.circadia.tracker.core.model

@JvmInline
value class PersonId(val value: String)

data class Person(
    val id: PersonId,
    val displayName: String,
    val colorSeed: Int,
    val sortIndex: Int,
    val createdAtUtcMillis: Long,
    val archivedAtUtcMillis: Long? = null,
)

enum class SleepState { AWAKE, ASLEEP }

enum class EventSource { WIDGET, APP, CORRECTION, IMPORT }

data class StateEvent(
    val id: String,
    val personId: PersonId,
    val state: SleepState,
    val occurredAtUtcMillis: Long,
    val timeZoneId: String,
    val source: EventSource,
    val recordedAtUtcMillis: Long,
    val supersedesEventId: String? = null,
    val voidedAtUtcMillis: Long? = null,
    val note: String? = null,
)

data class Interval(
    val personId: PersonId,
    val state: SleepState,
    val startUtcMillis: Long,
    val endUtcMillis: Long,
    val isOpen: Boolean,
    val startZoneId: String,
    val endZoneId: String,
    val startEventId: String
)

enum class EntitlementSource { FREE, SUBSCRIPTION, RESEARCH }

data class Entitlement(
    val source: EntitlementSource,
    val validUntilUtcMillis: Long?,
)

data class DailyMetrics(
    val personId: PersonId,
    val date: java.time.LocalDate,
    val totalSleepMillis: Long,
    val sleepEpisodes: Int,
    val maxAwakeMillis: Long,
    val sleepStartUtcMillis: Long?,
    val sleepEndUtcMillis: Long?
)
