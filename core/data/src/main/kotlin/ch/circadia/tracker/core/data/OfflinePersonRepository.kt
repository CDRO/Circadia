package ch.circadia.tracker.core.data

import ch.circadia.tracker.core.database.PersonDao
import ch.circadia.tracker.core.domain.PersonRepository
import ch.circadia.tracker.core.model.Person
import ch.circadia.tracker.core.model.PersonId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflinePersonRepository(
    private val personDao: PersonDao
) : PersonRepository {
    override fun getPersons(): Flow<List<Person>> = personDao.getPersons().map { list ->
        list.map { it.toDomain() }
    }

    override fun getPerson(id: PersonId): Flow<Person?> = personDao.getPerson(id.value).map { 
        it?.toDomain()
    }

    override suspend fun getPersonSync(id: PersonId): Person? = 
        personDao.getPersonSync(id.value)?.toDomain()

    override suspend fun savePerson(person: Person) {
        personDao.insertPerson(person.toEntity())
    }

    override suspend fun deletePerson(id: PersonId) {
        personDao.deletePerson(id.value)
    }
}
