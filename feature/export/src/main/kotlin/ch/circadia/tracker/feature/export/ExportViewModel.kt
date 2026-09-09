package ch.circadia.tracker.feature.export

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.circadia.tracker.core.domain.*
import ch.circadia.tracker.core.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ExportUiState {
    data object Idle : ExportUiState
    data object Loading : ExportUiState
    data class Success(val message: String) : ExportUiState
    data class Error(val message: String) : ExportUiState
}

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val stateEventRepository: StateEventRepository,
    private val csvExportUseCase: CsvExportUseCase,
    private val jsonExportUseCase: JsonExportUseCase,
    private val deriveIntervalsUseCase: DeriveIntervalsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExportUiState>(ExportUiState.Idle)
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    fun exportEventsCsv(): Flow<String> = flow {
        val persons = personRepository.getPersons().first()
        val allEvents = persons.flatMap { stateEventRepository.getEvents(it.id).first() }
        emit(csvExportUseCase.exportEvents(allEvents))
    }

    fun exportIntervalsCsv(): Flow<String> = flow {
        val persons = personRepository.getPersons().first()
        val allIntervals = persons.flatMap { person ->
            val events = stateEventRepository.getEvents(person.id).first()
            deriveIntervalsUseCase(events, System.currentTimeMillis())
        }
        emit(csvExportUseCase.exportIntervals(allIntervals))
    }

    fun exportJson(): Flow<String> = flow {
        val persons = personRepository.getPersons().first()
        val allEvents = persons.flatMap { stateEventRepository.getEvents(it.id).first() }
        emit(jsonExportUseCase.exportToJson(persons, allEvents))
    }

    fun importJson(content: String) {
        viewModelScope.launch {
            _uiState.value = ExportUiState.Loading
            try {
                val data = jsonExportUseCase.importFromJson(content)
                
                // Save persons first
                data.persons.forEach { personRepository.savePerson(it) }
                
                // Save events with source = IMPORT
                data.events.forEach { event ->
                    stateEventRepository.addEvent(event.copy(source = EventSource.IMPORT))
                }
                
                _uiState.value = ExportUiState.Success("Import erfolgreich")
            } catch (e: Exception) {
                _uiState.value = ExportUiState.Error("Import fehlgeschlagen: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uiState.value = ExportUiState.Idle
    }
    
    fun onExportStarted() {
        _uiState.value = ExportUiState.Loading
    }
    
    fun onExportFinished(success: Boolean) {
        _uiState.value = if (success) {
            ExportUiState.Success("Export erfolgreich")
        } else {
            ExportUiState.Error("Export fehlgeschlagen")
        }
    }
}
