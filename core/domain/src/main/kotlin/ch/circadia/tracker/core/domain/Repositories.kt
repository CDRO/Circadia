package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.Entitlement
import ch.circadia.tracker.core.model.Person
import ch.circadia.tracker.core.model.PersonId
import ch.circadia.tracker.core.model.StateEvent
import kotlinx.coroutines.flow.Flow

interface PersonRepository {
    fun getPersons(): Flow<List<Person>>
    fun getPerson(id: PersonId): Flow<Person?>
    suspend fun getPersonSync(id: PersonId): Person?
    suspend fun savePerson(person: Person)
    suspend fun deletePerson(id: PersonId)
}

interface StateEventRepository {
    fun getEvents(personId: PersonId): Flow<List<StateEvent>>
    suspend fun getLatestEvent(personId: PersonId): StateEvent?
    suspend fun addEvent(event: StateEvent)
    suspend fun voidEvent(eventId: String, voidedAt: Long)
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
}
