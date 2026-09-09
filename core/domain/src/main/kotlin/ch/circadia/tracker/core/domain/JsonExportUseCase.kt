package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CircadiaExport(
    val schemaVersion: Int,
    val persons: List<Person>,
    val events: List<StateEvent>
)

class JsonExportUseCase {
    private val json = Json { prettyPrint = true }

    fun exportToJson(persons: List<Person>, events: List<StateEvent>): String {
        val export = CircadiaExport(
            schemaVersion = 1,
            persons = persons,
            events = events
        )
        return json.encodeToString(export)
    }

    fun importFromJson(content: String): CircadiaExport {
        return json.decodeFromString(content)
    }
}
