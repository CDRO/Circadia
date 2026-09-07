package ch.circadia.tracker.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import ch.circadia.tracker.core.designsystem.CircadiaTheme
import ch.circadia.tracker.core.domain.PersonRepository
import ch.circadia.tracker.core.domain.WidgetBindingRepository
import ch.circadia.tracker.core.model.Person
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {

    @Inject lateinit var personRepository: PersonRepository
    @Inject lateinit var widgetBindingRepository: WidgetBindingRepository

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setResult(RESULT_CANCELED)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            CircadiaTheme {
                val persons by personRepository.getPersons().collectAsState(initial = null)

                Scaffold { padding ->
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        when (val list = persons) {
                            null -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            else -> {
                                if (list.isEmpty()) {
                                    Button(
                                        onClick = { 
                                            val intent = Intent("ch.circadia.tracker.action.ONBOARDING")
                                            startActivity(intent)
                                        },
                                        modifier = Modifier.align(Alignment.Center)
                                    ) {
                                        Text("Person anlegen")
                                    }
                                } else {
                                    LazyColumn {
                                        items(list) { person ->
                                            ListItem(
                                                headlineContent = { Text(person.displayName) },
                                                modifier = Modifier.clickable {
                                                    onPersonSelected(person)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun onPersonSelected(person: Person) {
        lifecycleScope.launch {
            widgetBindingRepository.bindWidget(appWidgetId, person.id)

            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(RESULT_OK, resultValue)

            UpdateWidgetReceiver.updateAll(this@WidgetConfigActivity)
            finish()
        }
    }
}
