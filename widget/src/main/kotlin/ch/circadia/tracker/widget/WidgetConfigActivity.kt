package ch.circadia.tracker.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ch.circadia.tracker.core.designsystem.CircadiaTheme
import ch.circadia.tracker.core.designsystem.R as DesignR
import ch.circadia.tracker.core.model.Person
import ch.circadia.tracker.feature.persons.PersonsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            CircadiaTheme {
                WidgetConfigScreen(
                    onPersonSelected = { person ->
                        // TODO: Implement binding logic
                        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        setResult(RESULT_OK, resultValue)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetConfigScreen(
    onPersonSelected: (Person) -> Unit,
    viewModel: PersonsViewModel = hiltViewModel()
) {
    val persons by viewModel.persons.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(DesignR.string.persons_title)) }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(persons) { person ->
                ListItem(
                    headlineContent = { Text(person.displayName) },
                    modifier = Modifier.fillMaxWidth(),
                    trailingContent = {
                        Button(onClick = { onPersonSelected(person) }) {
                            Text(stringResource(DesignR.string.persons_save))
                        }
                    }
                )
            }
            item {
                Button(
                    onClick = { /* TODO: Open PersonSetupActivity */ },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(stringResource(DesignR.string.persons_create))
                }
            }
        }
    }
}
