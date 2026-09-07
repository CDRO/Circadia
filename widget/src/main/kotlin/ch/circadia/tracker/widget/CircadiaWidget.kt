package ch.circadia.tracker.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.widget.RemoteViews
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import ch.circadia.tracker.core.domain.PersonRepository
import ch.circadia.tracker.core.domain.RecordStateEventUseCase
import ch.circadia.tracker.core.domain.StateEventRepository
import ch.circadia.tracker.core.domain.WidgetBindingRepository
import ch.circadia.tracker.core.model.EventSource
import ch.circadia.tracker.core.model.PersonId
import ch.circadia.tracker.core.model.SleepState
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CircadiaWidget : GlanceAppWidget() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun stateEventRepository(): StateEventRepository
        fun widgetBindingRepository(): WidgetBindingRepository
        fun personRepository(): PersonRepository
    }

    companion object {
        private val SMALL_SQUARE = DpSize(100.dp, 100.dp)
        private val HORIZONTAL_RECTANGLE = DpSize(200.dp, 100.dp)
        private val BIG_RECTANGLE = DpSize(200.dp, 200.dp)

        val personIdKey = ActionParameters.Key<String>("personId")
        val stateKey = ActionParameters.Key<String>("state")
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_SQUARE, HORIZONTAL_RECTANGLE, BIG_RECTANGLE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            WidgetEntryPoint::class.java
        )

        val manager = GlanceAppWidgetManager(context)
        val appWidgetId = manager.getAppWidgetId(id)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        val personId = entryPoint.widgetBindingRepository().getPersonIdForWidget(appWidgetId)

        if (personId == null) {
            provideContent {
                GlanceTheme {
                    ErrorContent("Neu zuordnen")
                }
            }
        } else {
            val latestEvent = entryPoint.stateEventRepository().getLatestEvent(personId)
            val person = entryPoint.personRepository().getPersonSync(personId)
            
            provideContent {
                GlanceTheme {
                    WidgetContent(context, personId, person?.displayName ?: "Unbekannt", latestEvent?.state)
                }
            }
        }
    }

    @Composable
    private fun ErrorContent(message: String) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = message,
                style = TextStyle(color = GlanceTheme.colors.onErrorContainer)
            )
        }
    }

    @Composable
    private fun WidgetContent(
        context: Context, 
        personId: PersonId, 
        personName: String, 
        lastState: SleepState?
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
                .background(GlanceTheme.colors.background)
        ) {
            Text(
                text = personName,
                style = TextStyle(color = GlanceTheme.colors.onBackground)
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AndroidRemoteViews(
                    remoteViews = RemoteViews(context.packageName, R.layout.text_clock_layout)
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    text = "Sonne",
                    onClick = actionRunCallback<RecordEventAction>(
                        actionParametersOf(
                            personIdKey to personId.value,
                            stateKey to SleepState.AWAKE.name
                        )
                    )
                )
                Spacer(modifier = GlanceModifier.width(8.dp))
                Button(
                    text = "Mond",
                    onClick = actionRunCallback<RecordEventAction>(
                        actionParametersOf(
                            personIdKey to personId.value,
                            stateKey to SleepState.ASLEEP.name
                        )
                    )
                )
            }
        }
    }
}

class RecordEventAction : ActionCallback {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RecordEventEntryPoint {
        fun recordStateEventUseCase(): RecordStateEventUseCase
        fun stateEventRepository(): StateEventRepository
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val personId = parameters[CircadiaWidget.personIdKey] ?: return
        val stateName = parameters[CircadiaWidget.stateKey] ?: return
        val state = SleepState.valueOf(stateName)

        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            RecordEventEntryPoint::class.java
        )

        val latest = entryPoint.stateEventRepository().getLatestEvent(PersonId(personId))
        if (latest?.state == state) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Bereits erfasst", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val useCase = entryPoint.recordStateEventUseCase()

        useCase(
            personId = PersonId(personId),
            state = state,
            source = EventSource.WIDGET
        )

        UpdateWidgetReceiver.updateAll(context)
    }
}

class CircadiaWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CircadiaWidget()
}
