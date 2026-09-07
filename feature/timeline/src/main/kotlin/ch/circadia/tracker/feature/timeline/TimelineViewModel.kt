package ch.circadia.tracker.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.circadia.tracker.core.domain.*
import ch.circadia.tracker.core.model.Interval
import ch.circadia.tracker.core.model.Person
import ch.circadia.tracker.core.model.PersonId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

sealed interface TimelineUiState {
    data object Loading : TimelineUiState
    data class Content(
        val persons: List<Person>,
        val selectedPersonIds: Set<PersonId>,
        val actogramDays: List<ActogramDay>,
        val useDoublePlot: Boolean
    ) : TimelineUiState
    data object Empty : TimelineUiState
    data class Error(val message: String) : TimelineUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val stateEventRepository: StateEventRepository,
    private val settingsRepository: SettingsRepository,
    private val deriveIntervalsUseCase: DeriveIntervalsUseCase
) : ViewModel() {

    private val _selectedPersonIds = MutableStateFlow<Set<PersonId>>(emptySet())
    
    val uiState: StateFlow<TimelineUiState> = combine(
        personRepository.getPersons(),
        _selectedPersonIds,
        settingsRepository.getDayBoundary(),
        settingsRepository.getUseDoublePlot()
    ) { persons, selectedIds, dayBoundary, useDoublePlot ->
        DataSnapshot(persons, selectedIds, dayBoundary, useDoublePlot)
    }.flatMapLatest { snapshot ->
        if (snapshot.persons.isEmpty()) {
            flowOf(TimelineUiState.Empty)
        } else {
            val effectiveSelectedIds = snapshot.selectedPersonIds.ifEmpty { setOf(snapshot.persons.first().id) }
            val personId = effectiveSelectedIds.first()
            
            stateEventRepository.getEvents(personId).map { events ->
                val intervals = deriveIntervalsUseCase(events, System.currentTimeMillis())
                val days = prepareActogramDays(intervals, snapshot.dayBoundary, snapshot.useDoublePlot)
                
                TimelineUiState.Content(
                    persons = snapshot.persons,
                    selectedPersonIds = effectiveSelectedIds,
                    actogramDays = days,
                    useDoublePlot = snapshot.useDoublePlot
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimelineUiState.Loading)

    private fun prepareActogramDays(
        intervals: List<Interval>, 
        dayBoundary: java.time.LocalTime,
        useDoublePlot: Boolean
    ): List<ActogramDay> {
        if (intervals.isEmpty()) return emptyList()
        
        val bucketing = DayBucketing(dayBoundary)
        val zoneId = ZoneId.systemDefault().id
        
        val days = mutableListOf<ActogramDay>()
        
        val firstStart = intervals.first().startUtcMillis
        val lastEnd = intervals.last().endUtcMillis
        
        var currentBucketStart = bucketing.getDayBucketStart(firstStart, zoneId)
        val finalEnd = bucketing.getDayBucketStart(lastEnd, zoneId)
        
        while (currentBucketStart <= finalEnd) {
            val bucket1End = bucketing.getNextBucketStart(currentBucketStart, zoneId)
            val rowEnd = if (useDoublePlot) {
                bucketing.getNextBucketStart(bucket1End, zoneId)
            } else {
                bucket1End
            }
            
            val dayIntervals = intervals.filter { 
                it.startUtcMillis < rowEnd && it.endUtcMillis > currentBucketStart
            }
            
            days.add(
                ActogramDay(
                    date = Instant.ofEpochMilli(currentBucketStart).atZone(ZoneId.of(zoneId)).toLocalDate(),
                    startTimeUtc = currentBucketStart,
                    endTimeUtc = rowEnd,
                    intervals = dayIntervals
                )
            )
            
            currentBucketStart = bucket1End
        }
        
        return days.reversed()
    }

    fun selectPerson(personId: PersonId) {
        _selectedPersonIds.value = setOf(personId)
    }

    fun setUseDoublePlot(use: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseDoublePlot(use)
        }
    }
}

private data class DataSnapshot(
    val persons: List<Person>,
    val selectedPersonIds: Set<PersonId>,
    val dayBoundary: java.time.LocalTime,
    val useDoublePlot: Boolean
)
