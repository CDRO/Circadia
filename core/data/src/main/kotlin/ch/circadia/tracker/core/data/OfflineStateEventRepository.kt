package ch.circadia.tracker.core.data

import ch.circadia.tracker.core.database.StateEventDao
import ch.circadia.tracker.core.domain.StateEventRepository
import ch.circadia.tracker.core.model.PersonId
import ch.circadia.tracker.core.model.StateEvent
import kotlinx.coroutines.flow.*

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

    override fun getEventChain(eventId: String): Flow<List<StateEvent>> = flow {
        val initialEvent = stateEventDao.getEvent(eventId)?.toDomain() ?: return@flow
        
        stateEventDao.getAllEvents(initialEvent.personId.value).map { list ->
            val all = list.map { it.toDomain() }
            val chain = mutableListOf<StateEvent>()
            
            var current: StateEvent? = all.find { it.id == eventId }
            while (current != null) {
                chain.add(current)
                val nextId = current.supersedesEventId
                current = if (nextId != null) all.find { it.id == nextId } else null
            }
            chain.toList()
        }.collect { emit(it) }
    }

    override suspend fun deleteAll() {
        stateEventDao.deleteAllEvents()
    }
}
