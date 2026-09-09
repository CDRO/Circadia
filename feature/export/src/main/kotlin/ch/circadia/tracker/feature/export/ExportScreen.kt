package ch.circadia.tracker.feature.export

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: ExportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var pendingExportType by remember { mutableStateOf<String?>(null) }
    
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                viewModel.onExportStarted()
                val content = when (pendingExportType) {
                    "events_csv" -> viewModel.exportEventsCsv().first()
                    "intervals_csv" -> viewModel.exportIntervalsCsv().first()
                    "json" -> viewModel.exportJson().first()
                    else -> ""
                }
                
                val success = try {
                    context.contentResolver.openOutputStream(uri)?.use { 
                        it.write(content.toByteArray())
                    }
                    true
                } catch (e: Exception) {
                    false
                }
                viewModel.onExportFinished(success)
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val content = context.contentResolver.openInputStream(uri)?.use {
                    it.readBytes().decodeToString()
                }
                if (content != null) {
                    viewModel.importJson(content)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(DesignR.string.export_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState is ExportUiState.Loading) {
                CircularProgressIndicator()
            }
            
            Button(
                onClick = {
                    pendingExportType = "events_csv"
                    createDocumentLauncher.launch("circadia_events.csv")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(DesignR.string.export_events_csv))
            }

            Button(
                onClick = {
                    pendingExportType = "intervals_csv"
                    createDocumentLauncher.launch("circadia_intervals.csv")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(DesignR.string.export_intervals_csv))
            }

            HorizontalDivider()

            Button(
                onClick = {
                    pendingExportType = "json"
                    createDocumentLauncher.launch("circadia_backup.json")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(DesignR.string.export_json))
            }

            OutlinedButton(
                onClick = {
                    openDocumentLauncher.launch(arrayOf("application/json"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(DesignR.string.export_import_json))
            }
            
            when (val state = uiState) {
                is ExportUiState.Success -> Text(state.message, color = MaterialTheme.colorScheme.primary)
                is ExportUiState.Error -> Text(state.message, color = MaterialTheme.colorScheme.error)
                else -> {}
            }
        }
    }
}
