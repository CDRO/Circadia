package ch.circadia.tracker.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.circadia.tracker.core.domain.*
import ch.circadia.tracker.core.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val stateEventRepository: StateEventRepository,
    private val deriveIntervalsUseCase: DeriveIntervalsUseCase,
    private val calculateSRIUseCase: CalculateSRIUseCase,
    private val calculateStabilityVariabilityUseCase: CalculateStabilityVariabilityUseCase,
    private val calculateMSFscUseCase: CalculateMSFscUseCase,
    private val calculateSocialJetlagUseCase: CalculateSocialJetlagUseCase
) : ViewModel() {

    private val _selectedPersonId = MutableStateFlow<PersonId?>(null)
    
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AnalyticsUiState> = combine(
        personRepository.getPersons(),
        _selectedPersonId
    ) { persons, selectedId ->
        val person = if (selectedId != null) persons.find { it.id == selectedId } else persons.firstOrNull()
        persons to person
    }.flatMapLatest { (persons, person) ->
        if (person == null) {
            flowOf(AnalyticsUiState.Empty)
        } else {
            stateEventRepository.getEvents(person.id).map { events ->
                val intervals = deriveIntervalsUseCase(events, System.currentTimeMillis())
                if (intervals.isEmpty()) return@map AnalyticsUiState.Empty
                
                val startUtc = intervals.first().startUtcMillis
                val dayCount = ((System.currentTimeMillis() - startUtc) / 86400000).toInt().coerceAtLeast(1)
                
                val sri = calculateSRIUseCase(intervals, startUtc, dayCount)
                val sv = calculateStabilityVariabilityUseCase(intervals, startUtc, dayCount)
                
                // Group work/free days for MSFsc
                val (workIntervals, freeIntervals) = intervals.partition { interval ->
                    val dayOfWeek = Instant.ofEpochMilli(interval.startUtcMillis).atZone(ZoneId.of(interval.startZoneId)).dayOfWeek.value
                    (person.workDays and (1 shl (dayOfWeek - 1))) != 0
                }
                
                val msfsc = calculateMSFscUseCase(workIntervals, freeIntervals)
                val socialJetlag = calculateSocialJetlagUseCase(workIntervals, freeIntervals)
                
                AnalyticsUiState.Content(
                    persons = persons,
                    selectedPerson = person,
                    sri = sri,
                    isMetric = sv?.isMetric,
                    ivMetric = sv?.ivMetric,
                    msfsc = msfsc,
                    socialJetlag = socialJetlag
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState.Loading)

    fun selectPerson(personId: PersonId) {
        _selectedPersonId.value = personId
    }
}

sealed interface AnalyticsUiState {
    data object Loading : AnalyticsUiState
    data class Content(
        val persons: List<Person>,
        val selectedPerson: Person,
        val sri: Int?,
        val isMetric: Double?,
        val ivMetric: Double?,
        val msfsc: Long?,
        val socialJetlag: Long?
    ) : AnalyticsUiState
    data object Empty : AnalyticsUiState
}
