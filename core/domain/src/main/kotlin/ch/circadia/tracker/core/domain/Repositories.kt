package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.*
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    fun getPersons(): Flow<List<Person>>
    fun getPerson(id: PersonId): Flow<Person?>
    suspend fun getPersonSync(id: PersonId): Person?
    suspend fun savePerson(person: Person)
    suspend fun deletePerson(id: PersonId)
    suspend fun deleteAll()
}

interface StateEventRepository {
    fun getEvents(personId: PersonId): Flow<List<StateEvent>>
    suspend fun getLatestEvent(personId: PersonId): StateEvent?
    suspend fun addEvent(event: StateEvent)
    suspend fun voidEvent(eventId: String, voidedAt: Long)
    fun getEventChain(eventId: String): Flow<List<StateEvent>>
    suspend fun deleteAll()
}

interface WidgetBindingRepository {
    suspend fun bindWidget(appWidgetId: Int, personId: PersonId)
    suspend fun unbindWidget(appWidgetId: Int)
    suspend fun getPersonIdForWidget(appWidgetId: Int): PersonId?
}

interface EntitlementRepository {
    fun current(): Flow<Entitlement>
    suspend fun updateEntitlement(entitlement: Entitlement)
}

interface SettingsRepository {
    fun getDayBoundary(): Flow<java.time.LocalTime>
    suspend fun setDayBoundary(time: java.time.LocalTime)
    fun getUseDoublePlot(): Flow<Boolean>
    suspend fun setUseDoublePlot(use: Boolean)
    fun getUseSideBySide(): Flow<Boolean>
    suspend fun setUseSideBySide(use: Boolean)
    fun getLastUploadedEventRecordedAt(): Flow<Long>
    suspend fun setLastUploadedEventRecordedAt(time: Long)
    fun isRevocationPending(): Flow<Boolean>
    suspend fun setRevocationPending(pending: Boolean)
}

interface ResearchRepository {
    fun getConsent(): Flow<ResearchConsent?>
    suspend fun saveConsent(consent: ResearchConsent)
    suspend fun revokeConsent()
}

interface ResearchUploadEndpoint {
    suspend fun uploadEvents(events: List<AnonymizedEvent>): Boolean
}
