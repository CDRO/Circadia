package ch.circadia.tracker.feature.timeline

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.circadia.tracker.core.designsystem.R as DesignR
import ch.circadia.tracker.core.model.*
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedInterval by remember { mutableStateOf<Interval?>(null) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(DesignR.string.timeline_title)) },
                actions = {
                    val state = uiState
                    if (state is TimelineUiState.Content) {
                        IconButton(onClick = { viewModel.setUseSideBySide(!state.useSideBySide) }) {
                            Icon(
                                imageVector = if (state.useSideBySide) Icons.Default.ViewAgenda else Icons.Default.ViewColumn,
                                contentDescription = if (state.useSideBySide) "Overlay" else "Side-by-side"
                            )
                        }
                        IconButton(onClick = { viewModel.setUseDoublePlot(!state.useDoublePlot) }) {
                            Icon(
                                imageVector = if (state.useDoublePlot) Icons.Default.ViewAgenda else Icons.Default.ViewStream,
                                contentDescription = if (state.useDoublePlot) "Single Plot" else "Double Plot"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                TimelineUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                TimelineUiState.Empty -> Text(stringResource(DesignR.string.timeline_empty), modifier = Modifier.align(Alignment.Center))
                is TimelineUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                is TimelineUiState.Content -> {
                    Column {
                        PersonSelector(
                            persons = state.persons,
                            selectedIds = state.selectedPersonIds,
                            onPersonToggle = { viewModel.togglePersonSelection(it.id) }
                        )
                        
                        if (state.isLimited) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Text(
                                    text = "Kostenlose Version: Nur die letzten 7 Tage sichtbar.",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                ActogramRenderer(
                                    days = state.actogramDays,
                                    selectedPersonIds = state.selectedPersonIds.toList(),
                                    useSideBySide = state.useSideBySide,
                                    onIntervalClick = { selectedInterval = it }
                                )
                            }
                            
                            item {
                                MetricsSummary(
                                    persons = state.persons,
                                    selectedIds = state.selectedPersonIds,
                                    latestMetrics = state.actogramDays.firstOrNull()?.metrics ?: emptyList()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedInterval != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedInterval = null },
            sheetState = sheetState
        ) {
            val history by viewModel.getEventHistory(selectedInterval!!.startEventId).collectAsState(initial = emptyList())
            
            CorrectionSheetContent(
                interval = selectedInterval!!,
                history = history,
                onDismiss = { selectedInterval = null },
                onSave = { time, note ->
                    viewModel.saveCorrection(selectedInterval!!, time, note)
                    selectedInterval = null
                }
            )
        }
    }
}

@Composable
fun CorrectionSheetContent(
    interval: Interval,
    history: List<StateEvent>,
    onDismiss: () -> Unit,
    onSave: (Long, String?) -> Unit
) {
    val context = LocalContext.current
    val initialZdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(interval.startUtcMillis), ZoneId.of(interval.startZoneId))
    var selectedTime by remember { mutableStateOf(initialZdt.toLocalTime()) }
    var note by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(stringResource(DesignR.string.timeline_correction_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        
        val stateText = if (interval.state == SleepState.ASLEEP) {
            stringResource(DesignR.string.timeline_state_asleep)
        } else {
            stringResource(DesignR.string.timeline_state_awake)
        }
        Text("${stringResource(DesignR.string.timeline_correction_state)}: $stateText")
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(onClick = {
            TimePickerDialog(
                context,
                { _, hour, minute -> selectedTime = LocalTime.of(hour, minute) },
                selectedTime.hour,
                selectedTime.minute,
                DateFormat.is24HourFormat(context)
            ).show()
        }) {
            Text("${stringResource(DesignR.string.timeline_correction_time)}: $selectedTime")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(DesignR.string.timeline_correction_note)) },
            modifier = Modifier.fillMaxWidth()
        )
        
        if (history.size > 1) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(DesignR.string.timeline_history), style = MaterialTheme.typography.titleSmall)
            history.forEach { event ->
                val time = Instant.ofEpochMilli(event.occurredAtUtcMillis).atZone(ZoneId.of(event.timeZoneId)).format(DateTimeFormatter.ofPattern("HH:mm"))
                Text("- $time (${event.source}) ${event.note ?: ""}", style = MaterialTheme.typography.bodySmall)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onDismiss) { Text(stringResource(DesignR.string.common_cancel)) }
            Button(onClick = { 
                val newZdt = initialZdt.toLocalDate().atTime(selectedTime).atZone(initialZdt.zone)
                onSave(newZdt.toInstant().toEpochMilli(), note.ifBlank { null }) 
            }) { 
                Text(stringResource(DesignR.string.common_save)) 
            }
        }
    }
}

@Composable
fun PersonSelector(
    persons: List<Person>,
    selectedIds: Set<PersonId>,
    onPersonToggle: (Person) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = 0,
        edgePadding = 16.dp,
        divider = {},
        indicator = {}
    ) {
        persons.forEach { person ->
            FilterChip(
                selected = person.id in selectedIds,
                onClick = { onPersonToggle(person) },
                label = { Text(person.displayName) },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun MetricsSummary(
    persons: List<Person>,
    selectedIds: Set<PersonId>,
    latestMetrics: List<DailyMetrics>
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(stringResource(DesignR.string.analytics_summary_title), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        selectedIds.forEach { personId ->
            val person = persons.find { it.id == personId }
            val metrics = latestMetrics.find { it.personId == personId }
            
            if (person != null && metrics != null) {
                Text("${person.displayName}:", style = MaterialTheme.typography.bodyMedium)
                Text("- ${stringResource(DesignR.string.analytics_sleep_duration)}: ${metrics.totalSleepMillis / 3600000}h ${(metrics.totalSleepMillis % 3600000) / 60000}m")
                Text("- ${stringResource(DesignR.string.analytics_episodes)}: ${metrics.sleepEpisodes}")
                Text("- ${stringResource(DesignR.string.analytics_max_awake)}: ${metrics.maxAwakeMillis / 3600000}h")
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
