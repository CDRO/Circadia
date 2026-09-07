package ch.circadia.tracker.feature.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewStream
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.circadia.tracker.core.model.Person

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
                            onPersonSelected = { viewModel.selectPerson(it.id) }
                        )
                        
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                ActogramRenderer(days = state.actogramDays)
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
    selectedIds: Set<ch.circadia.tracker.core.model.PersonId>,
    onPersonSelected: (Person) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = persons.indexOfFirst { it.id in selectedIds }.coerceAtLeast(0),
        edgePadding = 16.dp
    ) {
        persons.forEach { person ->
            Tab(
                selected = person.id in selectedIds,
                onClick = { onPersonSelected(person) },
                text = { Text(person.displayName) }
            )
        }
    }
}
