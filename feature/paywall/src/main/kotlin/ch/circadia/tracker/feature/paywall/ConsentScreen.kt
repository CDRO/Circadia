package ch.circadia.tracker.feature.paywall

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.circadia.tracker.core.designsystem.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsentScreen(
    onComplete: (Boolean, Boolean) -> Unit,
    onCancel: () -> Unit
) {
    var consentSurvey by remember { mutableStateOf(false) }
    var consentData by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(DesignR.string.research_consent_title)) })
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
                        Text(stringResource(DesignR.string.common_cancel))
                    }
                    Button(
                        onClick = { onComplete(consentSurvey, consentData) },
                        modifier = Modifier.weight(1f),
                        enabled = consentSurvey || consentData
                    ) {
                        Text(stringResource(DesignR.string.research_agree))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(DesignR.string.research_consent_desc),
                style = MaterialTheme.typography.bodyLarge
            )

            ConsentItem(
                title = stringResource(DesignR.string.research_consent_survey),
                description = stringResource(DesignR.string.research_consent_survey_desc),
                checked = consentSurvey,
                onCheckedChange = { consentSurvey = it }
            )

            ConsentItem(
                title = stringResource(DesignR.string.research_consent_data),
                description = stringResource(DesignR.string.research_consent_data_desc),
                checked = consentData,
                onCheckedChange = { consentData = it }
            )
        }
    }
}

@Composable
fun ConsentItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
