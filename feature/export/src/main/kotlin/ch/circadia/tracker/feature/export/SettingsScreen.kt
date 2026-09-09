package ch.circadia.tracker.feature.export

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.circadia.tracker.core.designsystem.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onDeleteAllData: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(DesignR.string.settings_delete_all_title)) },
            text = { Text(stringResource(DesignR.string.settings_delete_all_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAllData()
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text(stringResource(DesignR.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(DesignR.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(DesignR.string.settings_title)) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(stringResource(DesignR.string.settings_delete_all_button), color = Color.White)
            }
        }
    }
}
