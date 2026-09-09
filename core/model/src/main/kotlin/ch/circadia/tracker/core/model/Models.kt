package ch.circadia.tracker.core.model

import kotlinx.serialization.Serializable

@Serializable
@JvmInline
value class PersonId(val value: String)

@Serializable
data class Person(
    val id: PersonId,
    val displayName: String,
    val colorSeed: Int,
    val sortIndex: Int,
    val createdAtUtcMillis: Long,
    val archivedAtUtcMillis: Long? = null,
    val workDays: Int = 31 // Bitmask
)

@Serializable
enum class SleepState { AWAKE, ASLEEP }

@Serializable
enum class EventSource { WIDGET, APP, CORRECTION, IMPORT }

@Serializable
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

@Serializable
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

@Serializable
enum class EntitlementSource { FREE, SUBSCRIPTION, RESEARCH }

@Serializable
data class Entitlement(
    val source: EntitlementSource,
    val validUntilUtcMillis: Long?,
)

@Serializable
data class ResearchConsent(
    val id: String,
    val grantedAtUtcMillis: Long,
    val revokedAtUtcMillis: Long? = null,
    val surveyVersion: String,
    val consentTextHash: String,
    val participantPseudonym: String,
    val validUntilUtcMillis: Long,
    val consentSurvey: Boolean,
    val consentData: Boolean
)

@Serializable
data class DailyMetrics(
    val personId: PersonId,
    val date: String, // Use String for simplicity in serialization
    val totalSleepMillis: Long,
    val sleepEpisodes: Int,
    val maxAwakeMillis: Long,
    val sleepStartUtcMillis: Long?,
    val sleepEndUtcMillis: Long?
)
