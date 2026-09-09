package ch.circadia.tracker.core.domain

import ch.circadia.tracker.core.model.Interval
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class CalculateMidSleepUseCase {
    operator fun invoke(mainEpisode: Interval): Long {
        return mainEpisode.startUtcMillis + (mainEpisode.endUtcMillis - mainEpisode.startUtcMillis) / 2
    }

    /**
     * Gibt die Schlafmitte als lokale Uhrzeit zurueck.
     */
    fun getLocalMidSleep(midSleepUtc: Long, zoneId: String): LocalTime {
        val zdt = Instant.ofEpochMilli(midSleepUtc).atZone(ZoneId.of(zoneId))
        return zdt.toLocalTime()
    }
}
