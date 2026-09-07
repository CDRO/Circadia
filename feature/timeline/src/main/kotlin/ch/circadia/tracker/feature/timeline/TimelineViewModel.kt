package ch.circadia.tracker.feature.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.circadia.tracker.core.designsystem.ChartColors
import ch.circadia.tracker.core.domain.*
import ch.circadia.tracker.core.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

sealed interface TimelineUiState {
    data object Loading : TimelineUiState
    data class Content(
        val persons: List<Person>,
        val selectedPersonIds: Set<PersonId>,
        val actogramDays: List<ActogramDay>,
        val useDoublePlot: Boolean,
        val useSideBySide: Boolean,
        val isLimited: Boolean
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
    private val entitlementRepository: EntitlementRepository,
    private val deriveIntervalsUseCase: DeriveIntervalsUseCase,
    private val calculateDailyMetricsUseCase: CalculateDailyMetricsUseCase
) : ViewModel() {

    private val _selectedPersonIds = MutableStateFlow<Set<PersonId>>(emptySet())
    
    val uiState: StateFlow<TimelineUiState> = combine(
        combine(
            personRepository.getPersons(),
            _selectedPersonIds,
            settingsRepository.getDayBoundary()
        ) { p, s, d -> Triple(p, s, d) },
        combine(
            settingsRepository.getUseDoublePlot(),
            settingsRepository.getUseSideBySide(),
            entitlementRepository.current()
        ) { d, sb, e -> Triple(d, sb, e) }
    ) { t1, t2 ->
        DataSnapshot(t1.first, t1.second, t1.third, t2.first, t2.second, t2.third)
    }.flatMapLatest { snapshot ->
        if (snapshot.persons.isEmpty()) {
            flowOf(TimelineUiState.Empty)
        } else {
            val effectiveSelectedIds = snapshot.selectedPersonIds.ifEmpty { setOf(snapshot.persons.first().id) }
            
            val personFlows = effectiveSelectedIds.map { personId ->
                stateEventRepository.getEvents(personId).map { events ->
                    val color = snapshot.persons.find { it.id == personId }?.colorSeed?.let {
                        ChartColors.colorForSeed(it)
                    } ?: ChartColors.Palette.first()
                    
                    deriveIntervalsUseCase(events, System.currentTimeMillis()).map { 
                        ColoredInterval(it, color)
                    }
                }
            }
            
            combine(personFlows) { allColoredIntervalsList ->
                val allColoredIntervals = allColoredIntervalsList.flatMap { it }
                
                var days = prepareActogramDays(allColoredIntervals, snapshot.dayBoundary, snapshot.useDoublePlot)
                
                val isLimited = snapshot.entitlement.source == EntitlementSource.FREE
                if (isLimited) {
                    days = days.take(7)
                }
                
                TimelineUiState.Content(
                    persons = snapshot.persons,
                    selectedPersonIds = effectiveSelectedIds,
                    actogramDays = days,
                    useDoublePlot = snapshot.useDoublePlot,
                    useSideBySide = snapshot.useSideBySide,
                    isLimited = isLimited
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimelineUiState.Loading)

    private fun prepareActogramDays(
        coloredIntervals: List<ColoredInterval>, 
        dayBoundary: java.time.LocalTime,
        useDoublePlot: Boolean
    ): List<ActogramDay> {
        if (coloredIntervals.isEmpty()) return emptyList()
        
        val bucketing = DayBucketing(dayBoundary)
        val zoneId = ZoneId.systemDefault().id
        
        val days = mutableListOf<ActogramDay>()
        
        val firstStart = coloredIntervals.minOf { it.interval.startUtcMillis }
        val lastEnd = coloredIntervals.maxOf { it.interval.endUtcMillis }
        
        var currentBucketStart = bucketing.getDayBucketStart(firstStart, zoneId)
        val finalEnd = bucketing.getDayBucketStart(lastEnd, zoneId)
        
        while (currentBucketStart <= finalEnd) {
            val bucket1End = bucketing.getNextBucketStart(currentBucketStart, zoneId)
            val rowEnd = if (useDoublePlot) {
                bucketing.getNextBucketStart(bucket1End, zoneId)
            } else {
                bucket1End
            }
            
            val dayIntervals = coloredIntervals.filter { 
                it.interval.startUtcMillis < rowEnd && it.interval.endUtcMillis > currentBucketStart
            }
            
            val dayMetrics = dayIntervals
                .groupBy { it.interval.personId }
                .mapNotNull { (_, personIntervals) ->
                    calculateDailyMetricsUseCase(personIntervals.map { it.interval }, currentBucketStart, bucket1End)
                }

            days.add(
                ActogramDay(
                    date = Instant.ofEpochMilli(currentBucketStart).atZone(ZoneId.of(zoneId)).toLocalDate(),
                    startTimeUtc = currentBucketStart,
                    endTimeUtc = rowEnd,
                    coloredIntervals = dayIntervals,
                    metrics = dayMetrics
                )
            )
            
            currentBucketStart = bucket1End
        }
        
        return days.reversed()
    }

    fun togglePersonSelection(personId: PersonId) {
        val current = _selectedPersonIds.value
        _selectedPersonIds.value = if (personId in current) {
            if (current.size > 1) current - personId else current // Keep at least one
        } else {
            current + personId
        }
    }

    fun setUseDoublePlot(use: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseDoublePlot(use)
        }
    }

    fun setUseSideBySide(use: Boolean) {
        viewModelScope.launch {
            settingsRepository.setUseSideBySide(use)
        }
    }

    fun saveCorrection(
        interval: Interval,
        newOccurredAt: Long,
        note: String?
    ) {
        viewModelScope.launch {
            val correction = StateEvent(
                id = UUID.randomUUID().toString(),
                personId = interval.personId,
                state = interval.state,
                occurredAtUtcMillis = newOccurredAt,
                timeZoneId = ZoneId.systemDefault().id,
                source = EventSource.CORRECTION,
                recordedAtUtcMillis = System.currentTimeMillis(),
                supersedesEventId = interval.startEventId,
                note = note
            )
            stateEventRepository.addEvent(correction)
            stateEventRepository.voidEvent(interval.startEventId, System.currentTimeMillis())
        }
    }

    fun getEventHistory(eventId: String): Flow<List<StateEvent>> =
        stateEventRepository.getEventChain(eventId)
}

private data class DataSnapshot(
    val persons: List<Person>,
    val selectedPersonIds: Set<PersonId>,
    val dayBoundary: java.time.LocalTime,
    val useDoublePlot: Boolean,
    val useSideBySide: Boolean,
    val entitlement: Entitlement
)
