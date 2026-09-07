package ch.circadia.tracker.feature.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.circadia.tracker.core.model.DailyMetrics
import ch.circadia.tracker.core.model.Person
import ch.circadia.tracker.core.model.PersonId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auswertung") },
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
                TimelineUiState.Empty -> Text("Keine Daten vorhanden", modifier = Modifier.align(Alignment.Center))
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
                                    useSideBySide = state.useSideBySide
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
        Text("Zusammenfassung (Letzter Tag)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        selectedIds.forEach { personId ->
            val person = persons.find { it.id == personId }
            val metrics = latestMetrics.find { it.personId == personId }
            
            if (person != null && metrics != null) {
                Text("${person.displayName}:", style = MaterialTheme.typography.bodyMedium)
                Text("- Schlafdauer: ${metrics.totalSleepMillis / 3600000}h ${(metrics.totalSleepMillis % 3600000) / 60000}m")
                Text("- Episoden: ${metrics.sleepEpisodes}")
                Text("- Längste Wachphase: ${metrics.maxAwakeMillis / 3600000}h")
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
