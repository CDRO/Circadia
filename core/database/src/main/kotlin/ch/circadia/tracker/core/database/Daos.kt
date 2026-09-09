package ch.circadia.tracker.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Query("SELECT * FROM persons WHERE archived_at IS NULL ORDER BY sort_index ASC")
    fun getPersons(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE id = :id")
    fun getPerson(id: String): Flow<PersonEntity?>

    @Query("SELECT * FROM persons WHERE id = :id")
    suspend fun getPersonSync(id: String): PersonEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: PersonEntity)

    @Query("UPDATE persons SET archived_at = :archivedAt WHERE id = :id")
    suspend fun archivePerson(id: String, archivedAt: Long)

    @Query("DELETE FROM persons WHERE id = :id")
    suspend fun deletePerson(id: String)

    @Query("DELETE FROM persons")
    suspend fun deleteAllPersons()
}

@Dao
interface StateEventDao {
    @Query("SELECT * FROM state_events WHERE person_id = :personId AND voided_at IS NULL ORDER BY occurred_at ASC")
    fun getEvents(personId: String): Flow<List<StateEventEntity>>

    @Query("SELECT * FROM state_events WHERE person_id = :personId AND voided_at IS NULL ORDER BY occurred_at DESC LIMIT 1")
    suspend fun getLatestEvent(personId: String): StateEventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: StateEventEntity)

    @Query("SELECT * FROM state_events WHERE id = :eventId")
    suspend fun getEvent(eventId: String): StateEventEntity?

    @Query("SELECT * FROM state_events WHERE person_id = :personId")
    fun getAllEvents(personId: String): Flow<List<StateEventEntity>>

    @Query("SELECT * FROM state_events WHERE recorded_at > :recordedAt AND voided_at IS NULL ORDER BY recorded_at ASC")
    suspend fun getEventsRecordedAfter(recordedAt: Long): List<StateEventEntity>

    @Query("UPDATE state_events SET voided_at = :voidedAt WHERE id = :eventId")
    suspend fun voidEvent(eventId: String, voidedAt: Long)

    @Query("DELETE FROM state_events")
    suspend fun deleteAllEvents()
}

@Dao
interface WidgetBindingDao {
    @Query("SELECT person_id FROM widget_bindings WHERE app_widget_id = :appWidgetId")
    suspend fun getPersonIdForWidget(appWidgetId: Int): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBinding(binding: WidgetBindingEntity)

    @Query("DELETE FROM widget_bindings WHERE app_widget_id = :appWidgetId")
    suspend fun deleteBinding(appWidgetId: Int)
}

@Dao
interface ResearchDao {
    @Query("SELECT * FROM research_consents ORDER BY granted_at DESC LIMIT 1")
    fun getLatestConsent(): Flow<ResearchConsentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsent(consent: ResearchConsentEntity)

    @Query("UPDATE research_consents SET revoked_at = :revokedAt WHERE revoked_at IS NULL")
    suspend fun revokeAllConsents(revokedAt: Long)
}
