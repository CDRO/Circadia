package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.Interval
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
        
        return withBom((listOf(header) + rows).joinToString("\n"))
    }

    fun exportIntervals(intervals: List<Interval>): String {
        val header = "personId,state,startUtcMillis,endUtcMillis,isOpen,startZoneId,endZoneId,startEventId"
        val rows = intervals.map { interval ->
            listOf(
                interval.personId.value,
                interval.state.name,
                interval.startUtcMillis.toString(),
                interval.endUtcMillis.toString(),
                interval.isOpen.toString(),
                interval.startZoneId,
                interval.endZoneId,
                interval.startEventId
            ).joinToString(",")
        }
        
        return withBom((listOf(header) + rows).joinToString("\n"))
    }

    private fun withBom(content: String): String {
        // UTF-8 with BOM for Excel
        return "\uFEFF" + content
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
