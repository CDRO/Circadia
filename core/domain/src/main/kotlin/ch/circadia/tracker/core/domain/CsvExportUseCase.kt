package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.StateEvent

class CsvExportUseCase {

    fun exportEvents(events: List<StateEvent>): String {
        val header = "id,personId,state,occurredAtUtcMillis,timeZoneId,source,recordedAtUtcMillis,supersedesEventId,voidedAtUtcMillis,note"
        val rows = events.map { event ->
            listOf(
                event.id,
                event.personId.value,
                event.state.name,
                event.occurredAtUtcMillis.toString(),
                event.timeZoneId,
                event.source.name,
                event.recordedAtUtcMillis.toString(),
                event.supersedesEventId ?: "",
                event.voidedAtUtcMillis?.toString() ?: "",
                escapeCsv(event.note ?: "")
            ).joinToString(",")
        }
        
        // UTF-8 with BOM for Excel
        val bom = "\uFEFF"
        return bom + (listOf(header) + rows).joinToString("\n")
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n")
        return if (needsQuotes) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
