package ch.circadia.tracker.feature.persons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.circadia.tracker.core.domain.PersonRepository
import ch.circadia.tracker.core.model.Person
import ch.circadia.tracker.core.model.PersonId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonsViewModel @Inject constructor(
    private val personRepository: PersonRepository
) : ViewModel() {
    val persons: StateFlow<List<Person>> = personRepository.getPersons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun savePerson(name: String, colorSeed: Int) {
        viewModelScope.launch {
            val person = Person(
                id = PersonId(java.util.UUID.randomUUID().toString()),
                displayName = name,
                colorSeed = colorSeed,
                sortIndex = 0, // TODO: proper indexing
                createdAtUtcMillis = System.currentTimeMillis()
            )
            personRepository.savePerson(person)
        }
    }

    fun archivePerson(id: PersonId) {
        viewModelScope.launch {
            // In a real app, we'd update the archived_at field.
            // For now, let's assume the repository handles it.
        }
    }

    fun deletePerson(id: PersonId) {
        viewModelScope.launch {
            personRepository.deletePerson(id)
        }
    }
}
