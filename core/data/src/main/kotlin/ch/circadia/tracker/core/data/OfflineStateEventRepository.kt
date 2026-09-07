package ch.circadia.tracker.core.data

import ch.circadia.tracker.core.database.StateEventDao
import ch.circadia.tracker.core.domain.StateEventRepository
import ch.circadia.tracker.core.model.PersonId
import ch.circadia.tracker.core.model.StateEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineStateEventRepository(
    private val stateEventDao: StateEventDao
) : StateEventRepository {
    override fun getEvents(personId: PersonId): Flow<List<StateEvent>> = 
        stateEventDao.getEvents(personId.value).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun getLatestEvent(personId: PersonId): StateEvent? = 
        stateEventDao.getLatestEvent(personId.value)?.toDomain()

    override suspend fun addEvent(event: StateEvent) {
        stateEventDao.insertEvent(event.toEntity())
    }

    override suspend fun voidEvent(eventId: String, voidedAt: Long) {
        stateEventDao.voidEvent(eventId, voidedAt)
    }
}
