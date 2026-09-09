package ch.circadia.tracker.core.data

import ch.circadia.tracker.core.database.PersonEntity
import ch.circadia.tracker.core.database.StateEventEntity
import ch.circadia.tracker.core.model.*

fun PersonEntity.toDomain() = Person(
    id = PersonId(id),
    displayName = displayName,
    colorSeed = colorSeed,
    sortIndex = sortIndex,
    createdAtUtcMillis = createdAt,
    archivedAtUtcMillis = archivedAt
)

fun Person.toEntity() = PersonEntity(
    id = id.value,
    displayName = displayName,
    colorSeed = colorSeed,
    sortIndex = sortIndex,
    createdAt = createdAtUtcMillis,
    archivedAt = archivedAtUtcMillis
)

fun StateEventEntity.toDomain() = StateEvent(
    id = id,
    personId = PersonId(personId),
    state = SleepState.valueOf(state),
    occurredAtUtcMillis = occurredAt,
    timeZoneId = timeZoneId,
    source = EventSource.valueOf(source),
    recordedAtUtcMillis = recordedAt,
    supersedesEventId = supersedesEventId,
    voidedAtUtcMillis = voidedAt,
    note = note
)

fun StateEvent.toEntity() = StateEventEntity(
    id = id,
    personId = personId.value,
    state = state.name,
    occurredAt = occurredAtUtcMillis,
    timeZoneId = timeZoneId,
    source = source.name,
    recordedAt = recordedAtUtcMillis,
    supersedesEventId = supersedesEventId,
    voidedAt = voidedAtUtcMillis,
    note = note
)

fun ch.circadia.tracker.core.database.ResearchConsentEntity.toDomain() = ResearchConsent(
    id = id,
    grantedAtUtcMillis = grantedAt,
    revokedAtUtcMillis = revokedAt,
    surveyVersion = surveyVersion,
    consentTextHash = consentTextHash,
    participantPseudonym = participantPseudonym,
    validUntilUtcMillis = validUntil,
    consentSurvey = consentSurvey,
    consentData = consentData
)

fun ResearchConsent.toEntity() = ch.circadia.tracker.core.database.ResearchConsentEntity(
    id = id,
    grantedAt = grantedAtUtcMillis,
    revokedAt = revokedAtUtcMillis,
    surveyVersion = surveyVersion,
    consentTextHash = consentTextHash,
    participantPseudonym = participantPseudonym,
    validUntil = validUntilUtcMillis,
    consentSurvey = consentSurvey,
    consentData = consentData
)
