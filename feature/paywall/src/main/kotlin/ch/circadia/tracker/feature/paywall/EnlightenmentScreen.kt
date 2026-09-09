package ch.circadia.tracker.feature.paywall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnlightenmentScreen(
    onContinue: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val content = remember {
        context.resources.openRawResource(R.raw.consent_draft_0).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).readText()
        }
    }

    val scrollState = rememberScrollState()
    val isAtBottom by remember {
        derivedStateOf {
            scrollState.value >= scrollState.maxValue && scrollState.maxValue >= 0
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Forschung: Aufklärung") })
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Abbrechen")
                    }
                    Button(
                        onClick = onContinue,
                        enabled = isAtBottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Weiter")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(text = content)
        }
    }
}
