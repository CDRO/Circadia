package ch.circadia.tracker.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "display_name") val displayName: String,
    @ColumnInfo(name = "color_seed") val colorSeed: Int,
    @ColumnInfo(name = "sort_index") val sortIndex: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "archived_at") val archivedAt: Long?
)

@Entity(
    tableName = "state_events",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["person_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["person_id", "occurred_at"], name = "idx_events_person_time"),
        Index(value = ["person_id", "voided_at", "occurred_at"], name = "idx_events_active")
    ]
)
data class StateEventEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "person_id") val personId: String,
    @ColumnInfo(name = "state") val state: String,
    @ColumnInfo(name = "occurred_at") val occurredAt: Long,
    @ColumnInfo(name = "time_zone_id") val timeZoneId: String,
    @ColumnInfo(name = "source") val source: String,
    @ColumnInfo(name = "recorded_at") val recordedAt: Long,
    @ColumnInfo(name = "supersedes_event_id") val supersedesEventId: String?,
    @ColumnInfo(name = "voided_at") val voidedAt: Long?,
    @ColumnInfo(name = "note") val note: String?
)

@Entity(
    tableName = "widget_bindings",
    foreignKeys = [
        ForeignKey(
            entity = PersonEntity::class,
            parentColumns = ["id"],
            childColumns = ["person_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WidgetBindingEntity(
    @PrimaryKey @ColumnInfo(name = "app_widget_id") val appWidgetId: Int,
    @ColumnInfo(name = "person_id") val personId: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(tableName = "research_consents")
data class ResearchConsentEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "granted_at") val grantedAt: Long,
    @ColumnInfo(name = "revoked_at") val revokedAt: Long?,
    @ColumnInfo(name = "survey_version") val surveyVersion: String,
    @ColumnInfo(name = "consent_text_hash") val consentTextHash: String,
    @ColumnInfo(name = "participant_pseudonym") val participantPseudonym: String,
    @ColumnInfo(name = "valid_until") val validUntil: Long
)
