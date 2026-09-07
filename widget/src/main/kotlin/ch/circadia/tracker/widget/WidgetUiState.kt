package ch.circadia.tracker.widget

import ch.circadia.tracker.core.model.PersonId
import ch.circadia.tracker.core.model.SleepState

enum class TimeMode { LIVE, HELD }

data class WidgetUiState(
    val personId: PersonId? = null,
    val personName: String = "",
    val timeMode: TimeMode = TimeMode.LIVE,
    val heldEpochMinute: Long? = null,
    val holdExpiresAtUtcMillis: Long? = null,
    val lastState: SleepState? = null
)
