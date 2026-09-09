package ch.circadia.tracker.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Survey(
    val id: String,
    val version: Int,
    val status: String, // "draft" | "final"
    val questions: List<SurveyQuestion>
)

@Serializable
data class SurveyQuestion(
    val id: String,
    val text: String,
    val type: SurveyQuestionType,
    val options: List<SurveyOption> = emptyList(),
    val isRequired: Boolean = false
)

@Serializable
enum class SurveyQuestionType {
    SINGLE_CHOICE, MULTI_CHOICE, RANGE, FREE_TEXT
}

@Serializable
data class SurveyOption(
    val id: String,
    val text: String
)

@Serializable
data class SurveyResponse(
    val surveyId: String,
    val surveyVersion: Int,
    val answers: Map<String, String> // questionId -> serialized answer
)
