package ch.circadia.tracker.core.data

import ch.circadia.tracker.core.domain.AnonymizedEvent
import ch.circadia.tracker.core.domain.ResearchUploadEndpoint

class NoopUploadEndpoint : ResearchUploadEndpoint {
    override suspend fun uploadEvents(events: List<AnonymizedEvent>): Boolean {
        // Just return true as if it succeeded
        return true
    }
}
