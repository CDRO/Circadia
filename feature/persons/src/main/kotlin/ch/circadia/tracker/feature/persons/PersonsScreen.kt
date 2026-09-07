package ch.circadia.tracker.feature.persons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.circadia.tracker.core.model.Person
import ch.circadia.tracker.core.designsystem.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonsScreen(
    viewModel: PersonsViewModel
) {
    val persons by viewModel.persons.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AddPersonDialog(
            onDismiss = { showDialog = false },
            onSave = { name ->
                viewModel.savePerson(name, 0)
                showDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(id = DesignR.string.persons_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = DesignR.string.persons_add_desc))
            }
        }
    ) { padding ->
        if (persons.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(id = DesignR.string.persons_empty))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(persons) { person ->
                    PersonItem(person, onDelete = { viewModel.deletePerson(person.id) })
                }
            }
        }
    }
}

@Composable
fun PersonItem(person: Person, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(person.displayName) },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Löschen")
            }
        }
    )
}

@Composable
fun AddPersonDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Person hinzufügen") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") }
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name) }, enabled = name.isNotBlank()) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
