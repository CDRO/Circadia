package ch.circadia.tracker.feature.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.circadia.tracker.core.designsystem.R as DesignR
import ch.circadia.tracker.core.model.Person
import ch.circadia.tracker.core.model.PersonId
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(DesignR.string.analytics_title)) })
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (val state = uiState) {
                AnalyticsUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                AnalyticsUiState.Empty -> Text(stringResource(DesignR.string.analytics_empty), modifier = Modifier.align(Alignment.Center))
                is AnalyticsUiState.Content -> {
                    Column {
                        SinglePersonSelector(
                            persons = state.persons,
                            selectedId = state.selectedPerson.id,
                            onPersonSelected = { viewModel.selectPerson(it.id) }
                        )
                        
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                MetricCard(
                                    title = "Sleep Regularity Index (SRI)",
                                    value = state.sri?.toString() ?: "N/A",
                                    description = "Gibt an, wie regelmäßig dein Schlaf-Wach-Rhythmus ist (0-100).",
                                    source = "Phillips et al., Sci Rep 2017"
                                )
                            }
                            item {
                                MetricCard(
                                    title = "Chronotyp (MSFsc)",
                                    value = state.msfsc?.let { 
                                        Instant.ofEpochMilli(it).atZone(ZoneId.of("UTC")).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                                    } ?: "N/A",
                                    description = "Deine Schlafmitte an freien Tagen, korrigiert um die Schlafschuld der Arbeitswoche.",
                                    source = "Roenneberg et al., 2004"
                                )
                            }
                            item {
                                MetricCard(
                                    title = "Sozialer Jetlag",
                                    value = state.socialJetlag?.let {
                                        "${it / 3600000}h ${(it % 3600000) / 60000}m"
                                    } ?: "N/A",
                                    description = "Differenz der Schlafmitte zwischen Arbeits- und Freitagen.",
                                    source = "Wittmann et al., 2006"
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
fun MetricCard(
    title: String,
    value: String,
    description: String,
    source: String
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        onClick = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Quelle: $source", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun SinglePersonSelector(
    persons: List<Person>,
    selectedId: PersonId,
    onPersonSelected: (Person) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = persons.indexOfFirst { it.id == selectedId }.coerceAtLeast(0),
        edgePadding = 16.dp
    ) {
        persons.forEach { person ->
            Tab(
                selected = person.id == selectedId,
                onClick = { onPersonSelected(person) },
                text = { Text(person.displayName) }
            )
        }
    }
}
