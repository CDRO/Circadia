package ch.circadia.tracker.feature.paywall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.circadia.tracker.core.designsystem.R as DesignR
import ch.circadia.tracker.core.model.*
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(
    onComplete: (SurveyResponse) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val survey = remember {
        context.assets.open("surveys/survey-draft-0.json").use { inputStream ->
            Json.decodeFromString<Survey>(inputStream.bufferedReader().readText())
        }
    }

    var answers by remember { mutableStateOf(mapOf<String, String>()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(DesignR.string.research_survey_title)) })
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
                        onClick = {
                            onComplete(SurveyResponse(survey.id, survey.version, answers))
                        },
                        enabled = survey.questions.all { !it.isRequired || answers.containsKey(it.id) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(DesignR.string.research_finish))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(survey.questions) { question ->
                QuestionItem(
                    question = question,
                    currentAnswer = answers[question.id],
                    onAnswerChanged = { answer ->
                        answers = answers + (question.id to answer)
                    }
                )
            }
        }
    }
}

@Composable
fun QuestionItem(
    question: SurveyQuestion,
    currentAnswer: String?,
    onAnswerChanged: (String) -> Unit
) {
    Column {
        Text(text = question.text, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        when (question.type) {
            SurveyQuestionType.SINGLE_CHOICE -> {
                question.options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentAnswer == option.id,
                            onClick = { onAnswerChanged(option.id) }
                        )
                        Text(
                            text = option.text,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            SurveyQuestionType.FREE_TEXT -> {
                OutlinedTextField(
                    value = currentAnswer ?: "",
                    onValueChange = onAnswerChanged,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(DesignR.string.research_survey_answer_label)) }
                )
            }
            else -> {
                Text("Fragetyp ${question.type} noch nicht implementiert")
            }
        }
    }
}
